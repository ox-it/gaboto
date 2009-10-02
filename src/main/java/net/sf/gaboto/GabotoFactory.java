/**
 * Copyright 2009 University of Oxford
 *
 * Written by Arno Mittelbach for the Erewhon Project
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the University of Oxford nor the names of its 
 *    contributors may be used to endorse or promote products derived from this 
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.sf.gaboto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;


import net.sf.gaboto.event.GabotoEvent;
import net.sf.gaboto.event.InsertionGabotoEvent;
import net.sf.gaboto.event.RemovalGabotoEvent;
import net.sf.gaboto.event.UpdateListener;
import net.sf.gaboto.time.TimeDimensionIndexer;
import net.sf.gaboto.time.TimeInstant;
import net.sf.gaboto.util.Performance;
import net.sf.gaboto.vocabulary.RDFContext;


import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.vocabulary.RDF;

import de.fuberlin.wiwiss.ng4j.NamedGraph;
import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
import de.fuberlin.wiwiss.ng4j.Quad;
import de.fuberlin.wiwiss.ng4j.db.NamedGraphSetDB;
import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;

/**
 * Factory class to create Gaboto objects.
 * 
 * 
 * @author Arno Mittelbach
 * @version 0.1
 */
public class GabotoFactory {
	
	private static Gaboto persistentGaboto = null;
	
	static Gaboto inMemoryGaboto = null;
	
	private static Model contextDescriptiontGraph = null;
	
  public static GabotoConfiguration config  = GabotoConfiguration.fromConfigFile();

  private static Hashtable<String,Gaboto> knownStores = new Hashtable<String,Gaboto>();
  private static Hashtable<String,GabotoSnapshot> knownSnapshots = new Hashtable<String,GabotoSnapshot>();
  
  /**
   * Returns the Gaboto configuration.
   * @return The Gaboto configuration.
   */
  public static GabotoConfiguration getConfig(){
    return config;
  }
  
	/**
	 * @param directoryName name of the store, normnally a diretory name
	 * @return a cached or newly minted Gaboto
	 */
	public static Gaboto getGaboto(String directoryName) { 
    if (directoryName == null)
      throw new NullPointerException();
    Gaboto it = null;
    synchronized(knownStores) {
      it = knownStores.get(directoryName);
      if (it == null)
        knownStores.put(directoryName, readPersistedGaboto(directoryName));
      it = knownStores.get(directoryName);
    }
    return it;
  }
	public static Gaboto refreshedGaboto(String directoryName) { 
    if (directoryName == null)
      throw new NullPointerException();
    Gaboto it = null;
    synchronized(knownStores) {
      knownStores.put(directoryName, readPersistedGaboto(directoryName));
      it = knownStores.get(directoryName);
    }
    return it;
  }
	/**
	 * @return a cached or newly minted GabotSnapshot
	 */
	public static GabotoSnapshot getSnapshot(String directoryName, TimeInstant timeInstant) { 
	  GabotoSnapshot it = null;
	  String key = directoryName + ":" + timeInstant.toString();
	  synchronized(knownSnapshots){
      it = knownSnapshots.get(key);
	    if (it == null) {
	      it = getGaboto(directoryName).getSnapshot(timeInstant);
	      knownSnapshots.put(key, it);
	    }
      System.err.println("");
	  }
	  return it;
	}
	
  private static Gaboto readPersistedGaboto(String directoryName) {
    return readPersistedGaboto(directoryName, Gaboto.GRAPH_FILE_NAME, Gaboto.CDG_FILE_NAME);
  }
  public static Gaboto readPersistedGaboto(String directoryName, String graphName, String contextName) {
    File graphs = new File(directoryName, graphName);
    File context = new File(directoryName, contextName);
    FileInputStream graphsFileInputStream; 
    FileInputStream contextFileInputStream; 
    try {
      graphsFileInputStream = new FileInputStream(graphs); 
      contextFileInputStream = new FileInputStream(context); 
    } catch (FileNotFoundException e) {
      throw new GabotoRuntimeException(e);
    }
    return readPersistedGaboto(graphsFileInputStream, contextFileInputStream);
  }
  public static Gaboto readPersistedGaboto(InputStream graphsInputStream, InputStream contextInputStream) {
    Gaboto g = getEmptyInMemoryGaboto();
    g.read(graphsInputStream, contextInputStream);
    g.recreateTimeDimensionIndex();
    return g;
  }

  /**
	 * Creates an empty in memory Gaboto model that is not linked to any persistent data store.
	 * 
	 * <p>
	 * A time dimension indexer is set.
	 * </p>
	 * 
	 * @return A new Gaboto object.
	 */
	public static Gaboto getEmptyInMemoryGaboto() {
		NamedGraphSet graphset = new NamedGraphSetImpl();
		return new Gaboto(createGlobalKnowledgeGraph(ModelFactory.createDefaultModel(), graphset), graphset, new TimeDimensionIndexer());
	}
	
	/**
	 * Creates a new in-memory Gaboto system that is kept in sync with the persistent Gaboto object.
	 * 
	 * <p>
	 * A time dimension indexer is set.
	 * </p>
	 * 
	 * <p>
	 * In memory objects should only be used for querying data.
	 * </p>
	 * 
	 * @return An Gaboto object with an in-memory store.
	 * 
	 * @see #getPersistentGaboto()
	 */
	@SuppressWarnings("unchecked")
	public static Gaboto getInMemoryGaboto() {
		if(inMemoryGaboto != null) {
			return inMemoryGaboto;
		}
		
		Gaboto po = getPersistentGaboto();
		
		// Create a new graphset and copy graphs
		NamedGraphSet graphset = new NamedGraphSetImpl();
		Iterator graphIt = po.getNamedGraphSet().listGraphs();
		while(graphIt.hasNext())
			graphset.createGraph(((NamedGraph)graphIt.next()).getGraphName());
    System.err.println("getInMemoryGaboto: have created graphs");
		
		Iterator it = po.getNamedGraphSet().findQuads(Node.ANY, Node.ANY, Node.ANY, Node.ANY);
		while(it.hasNext())
			graphset.addQuad((Quad)it.next());
    System.err.println("getInMemoryGaboto: have added quads");
		
		inMemoryGaboto = new Gaboto(createDbBackedCDG(), graphset, new TimeDimensionIndexer());
		
    System.err.println("getInMemoryGaboto: returning");
		return inMemoryGaboto;
	}
	

	/**
	 * Creates a new Gaboto model with a persistent data store.
	 * 
	 * <p>
	 * The connection to the database is configured in the {@link GabotoConfiguration}.
	 * </p>
	 *
	 * <p>
	 * A time dimension indexer is NOT set.
	 * </p>
	 * 
	 * <p>
	 * The Persistent Gaboto object should only be used to add and change data. For
	 * querying the data, an in-memory Gaboto should be used.
	 * </p>
	 * 
	 * <p>
	 * Update the persistent and the in-memory copy is updated.
	 * </p>
	 * 
	 * @return A new persistent Gaboto
	 * @see #getInMemoryGaboto()
	 * @see GabotoConfiguration
	 */
	public static Gaboto getPersistentGaboto() {
		// does it already exist?
		if(persistentGaboto != null)
			return persistentGaboto;
		
		// get config
		GabotoConfiguration c = GabotoFactory.getConfig();
		
		// create persistent gaboto
		String URL = c.getDbURL();
		String USER = c.getDbUser();
		String PW = c.getDbPassword();
		try {
			Class.forName(c.getDbDriver());
		} catch (ClassNotFoundException e1) {
      throw new RuntimeException(e1);
		}
		Connection connection = null;
    System.err.println("URL:"+URL + " USER:"+USER+ " PWD:"+PW);
		try {
      connection = DriverManager.getConnection(URL, USER, PW);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

		// Create a new graphset
    Performance.start("GabotoFactory new NamedGraphSetDB");
		NamedGraphSet graphset = new NamedGraphSetDB(connection);
    Performance.stop();
		
		// if graphset is empty, create special graphs
    Model cdg = createGlobalKnowledgeGraph(createDbBackedCDG(), graphset);

		// create object
    Performance.start("GabotoFactory new Gaboto");
		persistentGaboto = new Gaboto(cdg, graphset, new TimeDimensionIndexer());
    Performance.stop();
		
    Performance.start("GabotoFactory update listener");
		// attach update listener
		persistentGaboto.attachUpdateListener(new UpdateListener(){
			public void updateOccured(GabotoEvent e) {
        // try to cast event to insertion
        if(e instanceof InsertionGabotoEvent){
          InsertionGabotoEvent event = (InsertionGabotoEvent) e;
          if(event.getTimespan() != null)
            inMemoryGaboto.add(event.getTimespan(), event.getTriple());
          else
            inMemoryGaboto.add(event.getTriple());
        }
        // try to cast event to removal
        else if(e instanceof RemovalGabotoEvent){
          RemovalGabotoEvent event = (RemovalGabotoEvent) e;
          if(event.getQuad() != null)
            inMemoryGaboto.remove(event.getQuad());
          else if(event.getTimespan() != null && event.getTriple() != null )
            inMemoryGaboto.remove(event.getTimespan(), event.getTriple());
          else if(event.getTriple() != null)
            inMemoryGaboto.remove(event.getTriple());
        } else 
          throw new GabotoRuntimeException("Unexpected update type: " + e.getClass());
			}
		});
		
    Performance.stop();
		return persistentGaboto;
	}

	/**
	 * Adds the cdg to the graphset.
	 */
	private static Model createDbBackedCDG() {
	  System.err.println("In createCDG");
		if(contextDescriptiontGraph != null) {
			return contextDescriptiontGraph;
		}
		// get config
		GabotoConfiguration c = GabotoFactory.getConfig();
		
		String M_DB_URL         = c.getDbURL();
		String M_DB_USER        = c.getDbUser();
		String M_DB_PASSWD      = c.getDbPassword();
		String M_DB             = c.getDbEngineName();
		String M_DBDRIVER_CLASS = c.getDbDriver();

		// load the driver class just to provoke error
		try {
			Class.forName(M_DBDRIVER_CLASS);
		} catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
		}

		// create a database connection
		IDBConnection conn = new DBConnection(M_DB_URL, M_DB_USER, M_DB_PASSWD, M_DB);

		// create a model maker with the given connection parameters
		ModelMaker maker = ModelFactory.createModelRDBMaker(conn);
		
		// create cdg
		if(maker.hasModel("cdg"))
			contextDescriptiontGraph = maker.openModel("cdg");
		else
			contextDescriptiontGraph = maker.createModel("cdg");
		
		return contextDescriptiontGraph;
	}
	
	
	/**
	 * Adds the Global Knowledge Graph (GKG) to the graphset
	 * @param cdg Context Description Graph
	 * @param graphset
	 */
	private static Model createGlobalKnowledgeGraph(Model cdg, NamedGraphSet graphset) {
		
		if(graphset.containsGraph(config.getGlobalKnowledgeGraphURI()))
			throw new IllegalStateException("GlobalKnowledgeGraph already exists.");
		
		// Create Global Knowledge Graph
		graphset.createGraph(config.getGlobalKnowledgeGraphURI());
		
		// add Global Knowledge Graph to Context Description Graph
		cdg.getGraph().add(new Triple(
				Node.createURI(config.getGlobalKnowledgeGraphURI()),
				Node.createURI(RDF.type.getURI()),
				Node.createURI(RDFContext.GlobalKnowledgeGraph.getURI())
		));
		return cdg;
	}


}
