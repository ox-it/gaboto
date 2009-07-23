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
package org.oucs.gaboto.transformation;

import java.io.StringWriter;

import org.oucs.gaboto.entities.pool.GabotoEntityPool;
import org.oucs.gaboto.model.query.GabotoQuery;
import org.oucs.gaboto.model.query.UnsupportedQueryFormatException;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Factory to create {@link EntityPoolTransformer}s that transform entity pools into various RDF formats.
 * 
 * @author Arno Mittelbach
 * @version 0.1
 * @see EntityPoolTransformer
 */
public class RDFPoolTransformerFactory {

	/**
	 * Creates a new transformer object for the specified format.
	 * 
	 * <p>
	 * Can create transformer objects for any RDF format. See {@link GabotoQuery} for
	 * a description of all supported RDF formats.
	 * </p>
	 * 
	 * @param format The RDF format 
	 * @return A transformer object that transforms an entity pool into RDF.
	 * 
	 * @throws UnsupportedQueryFormatException
	 */
	public static EntityPoolTransformer getRDFPoolTransformer(final String format) throws UnsupportedQueryFormatException{
		if(format.equals(GabotoQuery.FORMAT_RDF_XML) ||
		   format.equals(GabotoQuery.FORMAT_RDF_XML_ABBREV) ||
		   format.equals(GabotoQuery.FORMAT_RDF_TURTLE) ||
		   format.equals(GabotoQuery.FORMAT_RDF_N_TRIPLE) ||
		   format.equals(GabotoQuery.FORMAT_RDF_N3)){

			// create transformer object
			return new EntityPoolTransformer(){
				public String transform(GabotoEntityPool pool) {
					Model model = pool.createJenaModel();
					
					StringWriter sWriter = new StringWriter();
					model.write(sWriter, format);
					return sWriter.toString();
				}
			   
		   };
		}
		
		throw new UnsupportedQueryFormatException(format);
	}
}
