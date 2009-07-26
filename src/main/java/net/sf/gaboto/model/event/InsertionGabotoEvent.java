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
package net.sf.gaboto.model.event;

import net.sf.gaboto.model.Gaboto;
import net.sf.gaboto.time.TimeSpan;


import com.hp.hpl.jena.graph.Triple;

/**
 * Describes that a new triple has been inserted into an Gaboto model.
 * 
 * @author Arno Mittelbach
 * @version 0.1
 */
public class InsertionGabotoEvent extends GabotoEvent {

	private TimeSpan timespan;
	private Triple triple;
	
	/**
	 * Constructs a new insertion event that describes that a triple was added to the gdg.
	 * 
	 * @param triple The triple that was added.
	 * @see Gaboto#getGlobalKnowledgeGraph()
	 */
	public InsertionGabotoEvent(Triple triple) {
		super();
		this.triple = triple;
	}

	/**
	 * Creates a new insertion event that describes that a triple was added to the graph described by the time span.
	 * 
	 * @param timespan The time span.
	 * @param triple The triple that was added.
	 */
	public InsertionGabotoEvent(TimeSpan timespan, Triple triple) {
		super();
		this.timespan = timespan;
		this.triple = triple;
	}

	
	/**
	 * Returns the time span that describes the graph the triple was added to.
	 * 
	 * @return Null (in case the triple was added to the gdg) or the time span describing the graph the tripple was added to.
	 */
	public TimeSpan getTimespan() {
		return timespan;
	}

	/**
	 * Returns the triple that was added to the Gaboto system.
	 * 
	 * @return The triple that was added to the Gaboto system.
	 */
	public Triple getTriple() {
		return triple;
	}
}
