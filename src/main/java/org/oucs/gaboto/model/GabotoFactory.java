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
package org.oucs.gaboto.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;

import org.oucs.gaboto.GabotoConfiguration;
import org.oucs.gaboto.GabotoLibrary;
import org.oucs.gaboto.exceptions.GabotoException;
import org.oucs.gaboto.model.events.GabotoEvent;
import org.oucs.gaboto.model.events.GabotoInsertionEvent;
import org.oucs.gaboto.model.events.GabotoRemovalEvent;
import org.oucs.gaboto.model.listener.UpdateListener;
import org.oucs.gaboto.timedim.index.SimpleTimeDimensionIndexer;
import org.oucs.gaboto.util.Performance;
import org.oucs.gaboto.vocabulary.RDFCON;

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
	
	private static Gaboto inMemoryGaboto = null;
	
	/* Context Dependent Graph */
	private static Model cdg = null;
	
	
	/**
	 * Creates an empty in memory Gaboto model that is not linked to any persistent data store.
	 * 
	 * <p>
	 * A time dimension indexer is set.
	 * </p>
	 * 
	 * @return A new Gaboto object.
	 */
	public static Gaboto getEmptyInMemoryGaboto(){
		// Create a new graphset and copy graphs
		NamedGraphSet graphset = new NamedGraphSetImpl();
		
		// create none db backed up cdg
		cdg = ModelFactory.createDefaultModel();
		
		// get config
		GabotoConfiguration config = GabotoLibrary.getConfig();
		
		// if graphset is empty, create special graphs
		if(! graphset.containsGraph(config.getGKG()))
			createGKG(graphset);
		
		Gaboto test = new Gaboto(cdg, graphset, new SimpleTimeDimensionIndexer());

		return test;
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
	 * @throws GabotoException
	 * 
	 * @see #getPersistentGaboto()
	 */
	@SuppressWarnings("unchecked")
	public static Gaboto getInMemoryGaboto() {
		if(inMemoryGaboto != null)
			return inMemoryGaboto;
		
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
		
		inMemoryGaboto = new Gaboto(createCDG(), graphset, new SimpleTimeDimensionIndexer());
		
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
	 * @return A new persistent Gaboto
	 * @throws GabotoException 
	 * @see #getInMemoryGaboto()
	 * @see GabotoConfiguration
	 */
	public static Gaboto getPersistentGaboto() {
		// does it already exist?
		if(persistentGaboto != null)
			return persistentGaboto;
		
		// get config
		GabotoConfiguration config = GabotoLibrary.getConfig();
		
		// create persistent gaboto
		String URL = config.getDbURL();
		String USER = config.getDbUser();
		String PW = config.getDbPassword();
		try {
			Class.forName(config.getDbDriver());
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
		if(! graphset.containsGraph(config.getGKG()))
			createGKG(graphset);

		// create object
    Performance.start("GabotoFactory new Gaboto");
		persistentGaboto = new Gaboto(createCDG(), graphset, new SimpleTimeDimensionIndexer());
    Performance.stop();
		
    Performance.start("GabotoFactory update listener");
		// attach update listener
		persistentGaboto.attachUpdateListener(new UpdateListener(){
			public void updateOccured(GabotoEvent e) {
				if(inMemoryGaboto instanceof Gaboto){
					
					// try to cast event to insertion
					if(e instanceof GabotoInsertionEvent){
						GabotoInsertionEvent event = (GabotoInsertionEvent) e;
						if(event.getTimespan() != null)
							inMemoryGaboto.add(event.getTimespan(), event.getTriple());
						else
							inMemoryGaboto.add(event.getTriple());
					}
					// try to cast event to removal
					else if(e instanceof GabotoRemovalEvent){
						GabotoRemovalEvent event = (GabotoRemovalEvent) e;
						
						if(event.getQuad() != null)
							inMemoryGaboto.remove(event.getQuad());
						else if(event.getTimespan() != null && event.getTriple() != null )
							inMemoryGaboto.remove(event.getTimespan(), event.getTriple());
						else if(event.getTriple() != null)
							inMemoryGaboto.remove(event.getTriple());
					}

				}
			}
		});
		
    Performance.stop();
		return persistentGaboto;
	}

	/**
	 * Adds the cdg to the graphset.
	 */
	private static Model createCDG() {
		if(cdg != null)
			return cdg;
		
		// get config
		GabotoConfiguration config = GabotoLibrary.getConfig();
		
		String M_DB_URL         = config.getDbURL();
		String M_DB_USER        = config.getDbUser();
		String M_DB_PASSWD      = config.getDbPassword();
		String M_DB             = config.getDbEngineName();
		String M_DBDRIVER_CLASS = config.getDbDriver();

		// load the the driver class
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
			cdg = maker.openModel("cdg");
		else
			cdg = maker.createModel("cdg");
		
		return cdg;
	}
	
	
	/**
	 * adds the gkgg to the graphset
	 * @param graphset
	 */
	private static void createGKG(NamedGraphSet graphset) {
		// get config
		GabotoConfiguration config = GabotoLibrary.getConfig();
		
		if(graphset.containsGraph(config.getGKG()))
			throw new IllegalStateException("GKG already exists.");
		
		// Create gkg
		graphset.createGraph(config.getGKG());
		
		// add gkg to cdg
		createCDG().getGraph().add(new Triple(
				Node.createURI(config.getGKG()),
				Node.createURI(RDF.type.getURI()),
				Node.createURI(RDFCON.GlobalKnowledgeGraph.getURI())
		));		
	}
	
}
