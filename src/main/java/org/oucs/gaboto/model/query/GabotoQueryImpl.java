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
package org.oucs.gaboto.model.query;

import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.oucs.gaboto.GabotoFactory;
import org.oucs.gaboto.entities.pool.GabotoEntityPool;
import org.oucs.gaboto.entities.pool.GabotoEntityPoolConfiguration;
import org.oucs.gaboto.exceptions.GabotoRuntimeException;
import org.oucs.gaboto.model.Gaboto;
import org.oucs.gaboto.transformation.RDFPoolTransformerFactory;
import org.oucs.gaboto.transformation.json.JSONPoolTransformer;
import org.oucs.gaboto.transformation.kml.KMLPoolTransformer;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Provides a base class to easily write queries against the Gaboto system 
 * that can then be transformed into different output formats.
 * 
 * 
 * @author Arno Mittelbach
 * @version 0.2
 */
abstract public class GabotoQueryImpl implements GabotoQuery {
	
	private static Logger logger = Logger.getLogger(GabotoQueryImpl.class.getName());
	
	private boolean prepared = false;
	
	/**
	 * Describes that a Query creates a Jena {@link Model}
	 */
	protected final static int RESULT_TYPE_MODEL = 1;
	
	/**
	 * Describes that a Query creates an {@link GabotoEntityPool}
	 */
	protected final static int RESULT_TYPE_ENTITY_POOL = 2;
	
	/**
	 * Stores a reference to the Gaboto system that is to be used.
	 */
	private Gaboto gaboto;
	
	/**
	 * Constructs the query and grabs an in-memory Gaboto object from the GabotoFactory.
	 * 
	 */
	public GabotoQueryImpl() {
		this.gaboto = GabotoFactory.getInMemoryGaboto();
	}
	
	/**
	 * Constructor that allows to specifically set the Gaboto object this query object 
	 * is working with.
	 * 
	 * @param gaboto
	 */
	public GabotoQueryImpl(Gaboto gaboto){
		this.gaboto = gaboto;
	}
	
	
	/**
	 * Returns the Gaboto model this query is working on.
	 *  
	 * @return The Gaboto model this query is working on.
	 */
	public Gaboto getGaboto(){
		return gaboto;
	}
	
	/**
	 * Sets the Gaboto model this query should be working on.
	 * @param gaboto The Gaboto model this query should be working on.
	 */
	public void setGaboto(Gaboto gaboto){
		this.gaboto = gaboto;
	}
	
	/**
	 * Defines whether we are working with a EntityPool or a model.
	 * 
	 * @return The return type of our execute method.
	 * 
	 * @see #RESULT_TYPE_ENTITY_POOL
	 * @see #RESULT_TYPE_MODEL
	 */
	abstract public int getResultType();
	
	/**
	 * Performs the actual query work and returns either a Jena {@link Model} or an {@link GabotoEntityPool} as defined by {@link #getResultType()}.
	 * 
	 * @return Either a Jena Model or an GabotoEntityPool.
	 * 
	 * @see #getResultType()
	 * @see #execute(String)
	 */
	abstract protected Object execute();
	
	
	final public void prepare() throws QueryAlreadyPreparedException {
		if(prepared)
			throw new QueryAlreadyPreparedException();

		// call prepare method
		doPrepare();
		
		// set flag
		prepared = true;
	}

	abstract protected void doPrepare();

	final public boolean isPrepared(){
		return prepared;
	}
	
	/**
	 * Executes a query and transforms the result into the asked for output format (if supported). 
	 * 
	 * @param format The output format.
	 */
	public Object execute(String format) {
		logger.debug("Execute query " + this.getClass().getName() + " with requested format: " + format);
		
		// prepare if it is not prepared
		if(! isPrepared())
			prepare();
		
		if(! isSupportedFormat(format))
			throw new UnsupportedQueryFormatException(format);
		
		Object result = execute();
		
		switch(getResultType()){
		case RESULT_TYPE_MODEL:
			return formatResult((Model) result, format);
		case RESULT_TYPE_ENTITY_POOL:
			return formatResult((GabotoEntityPool)result, format);
		default: 
			return result;
		}
	}
	
	/**
	 * Formats results of the type {@link GabotoEntityPool} into the specified output format.
	 * 
	 * @param pool The entity pool to be transformed.
	 * @param format The output format.
	 * 
	 * @return The transformed pool.
	 */
	protected Object formatResult(GabotoEntityPool pool, String format) {
		if(format.equals(GabotoQuery.FORMAT_JENA_MODEL))
			return pool.createJenaModel();

		if(format.equals(GabotoQuery.FORMAT_ENTITY_POOL))
			return pool;
		
		if(format.equals(GabotoQuery.FORMAT_RDF_XML) ||
		   format.equals(GabotoQuery.FORMAT_RDF_XML_ABBREV) ||
		   format.equals(GabotoQuery.FORMAT_RDF_TURTLE) ||
		   format.equals(GabotoQuery.FORMAT_RDF_N_TRIPLE) ||
		   format.equals(GabotoQuery.FORMAT_RDF_N3)){
				try {
					return RDFPoolTransformerFactory.getRDFPoolTransformer(format).transform(pool);
				} catch (UnsupportedQueryFormatException e) {
          new GabotoRuntimeException(e);
				}
		}
		
		if(format.equals(GabotoQuery.FORMAT_KML))
			return new KMLPoolTransformer().transform(pool);
		
		if(format.equals(GabotoQuery.FORMAT_JSON))
			return new JSONPoolTransformer().transform(pool);
		
		return formatResult_customFormat(pool, format);
	}
	
	/**
	 * Called if a supported output format was specified that cannot be handled by this abstract class.
	 * 
	 * <p>
	 * Queries can override this method to implement custom transformations.
	 * </p>
	 * 
	 * @param pool The pool to be transformed.
	 * @param format The output format.
	 * @return The pool per default.
	 */
	protected Object formatResult_customFormat(GabotoEntityPool pool, String format) {
		return pool;
	}

	/**
	 * Formats results of the type Jena {@link Model} into the specified output format.
	 * 
	 * @param model The Jena Model to be transformed.
	 * @param format The output format.
	 * 
	 * @return The formatted model.
	 */
	protected Object formatResult(Model model, String format) {
		if(format.equals(GabotoQuery.FORMAT_JENA_MODEL))
			return model;
		
		if(format.equals(GabotoQuery.FORMAT_ENTITY_POOL)){
			return GabotoEntityPool.createFrom(new GabotoEntityPoolConfiguration(getGaboto(), model));
		}

		if(format.equals(GabotoQuery.FORMAT_RDF_XML) ||
		   format.equals(GabotoQuery.FORMAT_RDF_XML_ABBREV) ||
		   format.equals(GabotoQuery.FORMAT_RDF_TURTLE) ||
		   format.equals(GabotoQuery.FORMAT_RDF_N_TRIPLE) ||
		   format.equals(GabotoQuery.FORMAT_RDF_N3)){
				StringWriter sWriter = new StringWriter();
				model.write(sWriter, format);
				return sWriter.toString();
		}
		
		if(format.equals(GabotoQuery.FORMAT_KML)){
      return new KMLPoolTransformer().transform(
              GabotoEntityPool.createFrom(new GabotoEntityPoolConfiguration(getGaboto(), model)));
    }
		
		if(format.equals(GabotoQuery.FORMAT_JSON)){
      return new JSONPoolTransformer().transform(
              GabotoEntityPool.createFrom(new GabotoEntityPoolConfiguration(getGaboto(), model)));
		}
		
		return formatResult_customFormat(model, format);
	}
	
	/**
	 * Called if a supported output format was specified that cannot be handled by this abstract class.
	 * 
	 * <p>
	 * Queries can override this method to implement custom transformations.
	 * </p>
	 * 
	 * @param model The model to be transformed.
	 * @param format The output format.
	 * @return The model per default.
	 */
	protected Object formatResult_customFormat(Model model, String format) {
		return model;
	}


	/**
	 * Tests if the query supports the passed output format.
	 * 
	 * @param format The format
	 * @return True, if the format is supported.
	 */
	public boolean isSupportedFormat(String format){
		for(String s : getSupportedFormats())
			if(s.equals(format))
				return true;
		return false; 		
	}

	/**
	 * Defines the possible output formats that this query supports.
	 * 
	 * @return An array with all output formats.
	 */
	public String[] getSupportedFormats() {
		return new String[]{
			GabotoQuery.FORMAT_JENA_MODEL,
			GabotoQuery.FORMAT_ENTITY_POOL,
			
			GabotoQuery.FORMAT_RDF_XML,
			GabotoQuery.FORMAT_RDF_XML_ABBREV,
			GabotoQuery.FORMAT_RDF_N3,
			GabotoQuery.FORMAT_RDF_N_TRIPLE,
			GabotoQuery.FORMAT_RDF_TURTLE,
		
      GabotoQuery.FORMAT_TEI_XML,
      GabotoQuery.FORMAT_KML,
			GabotoQuery.FORMAT_JSON
		};
	}

}
