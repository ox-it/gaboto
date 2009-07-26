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

import org.oucs.gaboto.model.Gaboto;
import org.oucs.gaboto.node.pool.EntityPool;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * An interface for classes that query the Gaboto system.
 * 
 * @author Arno Mittelbach
 * @version 0.1
 * 
 * @see GabotoQueryImpl
 */
public interface GabotoQuery {

	/**
	 * Standard RDF/XML
	 */
	public static String FORMAT_RDF_XML = "RDF/XML";
	
	/**
	 * Abbreviated RDF/XML
	 */
	public static String FORMAT_RDF_XML_ABBREV = "RDF/XML-ABBREV";
	
	/**
	 * RDF in the form of N-Triple
	 * <p><a href="http://www.dajobe.org/2004/01/turtle/">http://www.dajobe.org/2004/01/turtle/</a></p>
	 */
	public static String FORMAT_RDF_N_TRIPLE = "N-TRIPLE";
	
	/**
	 * RDF in the form of Turtle
	 * <p><a href="http://www.w3.org/2001/sw/RDFCore/ntriples/">http://www.w3.org/2001/sw/RDFCore/ntriples/</a></p>
	 */
	public static String FORMAT_RDF_TURTLE = "TURTLE";
	
	/**
	 * RDF in the form of Notation 3
	 * <p><a href="http://www.w3.org/DesignIssues/Notation3">http://www.w3.org/DesignIssues/Notation3</a></p>
	 */
	public static String FORMAT_RDF_N3 = "N3";
	
	/**
	 * A Jena {@link Model}
	 */
	public static String FORMAT_JENA_MODEL = "JENA_MODEL";
	
	/**
	 * An {@link EntityPool}
	 */
	public static String FORMAT_ENTITY_POOL = "GABOTO_ENTITY_POOL";
	
	/**
	 * TEI/XML
	 * <p><a href="http://www.tei-c.org">http://www.tei-c.org</a></p>
	 */
	public static String FORMAT_TEI_XML = "TEI/XML";
	
	/**
	 * KML
	 * <p><a href="http://code.google.com/apis/kml/documentation/">http://code.google.com/apis/kml/documentation/</a></p>
	 */
	public static String FORMAT_KML = "KML";
	
	/**
	 * JSON (JavaScript Object Notation)
	 * <p><a href="http://www.json.org/">http://www.json.org/</a></p>
	 */
	public static String FORMAT_JSON = "JSON";
	
	/**
	 * Returns a list of supported formats.
	 * 
	 * @return A string array of supported formats.
	 */
	public String[] getSupportedFormats();
	
	/**
	 * Should be called after the query is configured. 
	 * 
	 * @throws QueryAlreadyPreparedException
	 */
	public void prepare() throws QueryAlreadyPreparedException;
	
	/**
	 * Executes the query and returns the specified output format (if supported).
	 * 
	 * @param format The output format.
	 * @return The query results transformed into the specified output format.
	 * 
	 */
	public Object execute(String format);
	
	/**
	 * Sets the Gaboto model the query should work with.
	 * 
	 * @param gaboto The Gaboto model to query.
	 */
	public void setGaboto(Gaboto gaboto);
}
