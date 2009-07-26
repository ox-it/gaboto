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
package org.oucs.gaboto.node.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.oucs.gaboto.node.GabotoBean;
import org.oucs.gaboto.node.GabotoEntity;

/**
 * Used to annotate methods in {@link GabotoEntity}s that deal with complex properties.
 * 
 * <p>
 * Complex properties are properties that consist of 1 RDF triple where the subject
 * is an {@link GabotoEntity} and the object is a blank node. 
 * Several further triples may then use the blank node to describe the data.
 * </p>
 * 
 * <p>
 * A complex property needs to have a corresponding {@link GabotoBean} that 
 * represents it in Java. 
 * </p>
 * 
 * <p>
 * An example for a complex property would be the location of places:
 * <pre>
 * oxpdata:somePlace	oxp:hasLocation		_blankNode .
 * _blankNode			rdf:type			gml:Point ;
 * 						gml:pos				"42.34 -71.21" .
 * </pre>
 * </p>
 * 
 * @author Arno Mittelbach
 * @version 0.1
 * 
 * @see GabotoEntity
 * @see GabotoBean
 * @see BagComplexProperty
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ComplexProperty {
	
	/**
	 * Returns the URI of the corresponding property. 
	 * @return The URI of the corresponding property.
	 */
	public String value();
}
