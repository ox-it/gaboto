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
package org.oucs.gaboto.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;

import org.oucs.gaboto.GabotoFactory;
import org.oucs.gaboto.vocabulary.GabotoVocab;
import org.oucs.gaboto.vocabulary.RDFCON;

public class GabotoPredefinedQueries {
  private static String QUERY_LOCATION = "/queries/";

	private static String readQueryFile(String name) {
	  
		InputStream is = GabotoPredefinedQueries.class.getResourceAsStream(QUERY_LOCATION + name);
    if (is == null)
      throw new NullPointerException("Cannot open classpath resource " + QUERY_LOCATION + name);
		StringBuffer out = new StringBuffer();
	    byte[] b = new byte[4096];
	    try {
        for (int n; (n = is.read(b)) != -1;) 
            out.append(new String(b, 0, n));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
        
        return out.toString();
	}
	
	public static String getStandardPrefixes(){
		String prefixes =
			    "PREFIX dc:<http://purl.org/dc/elements/1.1/>\n" +
			   	"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			   	"PREFIX gaboto:<" + GabotoVocab.NS + ">\n" +
			   	"PREFIX data:<" + GabotoFactory.getConfig().getNSData() + ">\n" +
			  	"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" +
				  "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n" +
			  	"PREFIX xsd:<http://www.w3.org/2001/XMLSchema>\n" +
			  	"PREFIX rdfcon:<" + RDFCON.NS + ">\n" +
			  	"PREFIX owl-time:<http://www.w3.org/2006/time#>\n" +
			  	"PREFIX rdfg:<http://www.w3.org/2004/03/trix/rdfg-1/>\n";
		
		for(Entry<String, String> entry : GabotoFactory.getConfig().getNamespacePrefixes().entrySet())
			prefixes += "PREFIX " + entry.getKey() + ":<" + entry.getValue() + ">\n";
		
		return prefixes + "\n";
	}
	
	public static String getTimeInformationQuery(String graph){
  	String query = readQueryFile("gettimeinformation.txt");
		return String.format(query, "<" + graph + ">");
	}
		

	public static String getTimeDimensionIndexQuery(){
		return readQueryFile("timeindex.txt");
	}
}
