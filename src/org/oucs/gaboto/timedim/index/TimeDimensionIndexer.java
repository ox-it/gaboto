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
package org.oucs.gaboto.timedim.index;

import java.util.Collection;

import org.oucs.gaboto.exceptions.CorruptDataException;
import org.oucs.gaboto.exceptions.GabotoException;
import org.oucs.gaboto.model.Gaboto;
import org.oucs.gaboto.timedim.TimeInstant;
import org.oucs.gaboto.timedim.TimeSpan;

import com.hp.hpl.jena.rdf.model.Model;

import de.fuberlin.wiwiss.ng4j.NamedGraph;

/**
 * Provides an interface for time dimension indexer to work with {@link Gaboto}.
 * 
 * <p>
 * Querying the cdg (see {@link Gaboto#getContextDescriptionGraph()}) using SPARQL is
 * hardly possible (or at least not feasible). TimeDimensionIndexer relieve Gaboto of
 * that problem by parsing all time information into some Java representation and providing
 * methods to easily query that information.
 * </p>
 * 
 * @author Arno Mittelbach
 * @version 0.1
 * @see Gaboto
 */
public interface TimeDimensionIndexer {

	/**
	 * Builds the index.
	 * 
	 * @param graphset The graphset the index should be built upon.
	 * @throws GabotoException
	 */
	public void createIndex(Model cdg) throws CorruptDataException;
	
	/**
	 * Adds another graph to the index.
	 * 
	 * @param graph The graph's name.
	 * @param ts The graph's time span.
	 */
	public void add(String graph, TimeSpan ts);
	
	/**
	 * Adds another graph to the index.
	 * 
	 * @param graph The graph.
	 * @param ts The graph's time span.
	 */
	public void add(NamedGraph graph, TimeSpan ts);
	
	/**
	 * Returns the time span for a given graph
	 * 
	 * @param graphURI The graph's name.
	 * 
	 * @return The graph's time span (or null).
	 */
	public TimeSpan getTimeSpanFor(String graphURI);
	
	/**
	 * Returns all the graphs that hold information which is valid a the given point in time.
	 * 
	 * @param ti The time instant.
	 * 
	 * @return A collection of graph names.
	 */
	public Collection<String> getGraphsForInstant(TimeInstant ti);
	
	/**
	 * Returns all the graphs that hold information which is valid over a given time span.
	 * 
	 * @param ts The time span.
	 * 
	 * @return A collection of graph names.
	 */
	public Collection<String> getGraphsForDuration(TimeSpan ts);
}
