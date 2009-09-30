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
package net.sf.gaboto.time;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.sf.gaboto.Gaboto;
import net.sf.gaboto.IncoherenceException;
import net.sf.gaboto.util.GabotoPredefinedQueries;


import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import de.fuberlin.wiwiss.ng4j.NamedGraph;

/**
 * <p>
 * Querying the cdg (see {@link Gaboto#getContextDescriptionGraph()}) using SPARQL is
 * hardly possible (or at least not feasible). TimeDimensionIndexer relieves Gaboto of
 * that problem by parsing all time information into a Java representation and providing
 * methods to query that information.
 * </p>
 * NOTE This class does not use any proper data structure to do the task.
 * 
 * TODO think of a proper tree to do the indexing!
 * 
 * @author Arno Mittelbach
 *
 */
public class TimeDimensionIndexer {

	private Map<String, TimeSpan> lookup = new HashMap<String, TimeSpan>();
	
  /**
   * Builds the index.
   * 
   * @param graphset The graphset the index should be built upon.
   */
	public void createIndex(Model cdg) throws IncoherenceException {
	  System.err.println("Creating time index");
		String query = GabotoPredefinedQueries.getTimeDimensionIndexQuery();
		QueryExecution qe = QueryExecutionFactory.create( query, cdg );
		ResultSet rs = qe.execSelect();

	
		while(rs.hasNext()){
			QuerySolution qs = rs.nextSolution();
			
			// get variables
			RDFNode graphNode = qs.get("graph");
			RDFNode startYearNode = qs.get("beginDescYear");
			RDFNode startMonthNode = qs.get("beginDescMonth");
			RDFNode startDayNode = qs.get("beginDescDay");
			RDFNode durationYearNode = qs.get("durationYears");
			RDFNode durationMonthNode = qs.get("durationMonths");
			RDFNode durationDayNode = qs.get("durationDays");
			
			// extract graph
			String graph = ((Resource)graphNode).getURI();
			
			try{	
				// extract timespan
				TimeSpan ts = new TimeSpan();
				ts.setStartYear(((Literal)startYearNode).getInt());
				if(null != startMonthNode)
					ts.setStartMonth(((Literal)startMonthNode).getInt());
				if(startDayNode != null)
					ts.setStartDay(((Literal)startDayNode).getInt());
				
				if(durationYearNode != null)
					ts.setDurationYear(((Literal)durationYearNode).getInt());
				if(durationMonthNode != null)
					ts.setDurationMonth(((Literal)durationMonthNode).getInt());
				if(durationDayNode != null)
					ts.setDurationDay(((Literal)durationDayNode).getInt());
				
				add(graph, ts);
			} catch(IllegalArgumentException e){
				throw new IncoherenceException("The data seems to be corrupt. Can not load graph " + graph , e);
			}
		}
		
	}
	
  /**
   * Adds another graph to the index.
   * 
   * @param graph The graph's name.
   * @param ts The graph's time span.
   */
	public void add(String graph, TimeSpan ts){
		lookup.put(graph, ts);
	}
	
  /**
   * Adds another graph to the index.
   * 
   * @param graph The graph.
   * @param ts The graph's time span.
   */
	public void add(NamedGraph graph, TimeSpan ts){
		add(graph.getGraphName().getURI(), ts);
	}

  /**
   * Returns the time span for a given graph
   * 
   * @param graphURI The graph's name.
   * 
   * @return The graph's time span (or null).
   */
	public Collection<String> getGraphsForDuration(TimeSpan ts) {
		Set<String> graphs = new HashSet<String>();

		for(Entry<String, TimeSpan> entry : lookup.entrySet())
			if(entry.getValue().contains(ts))
				graphs.add(entry.getKey());
		
		return graphs; 
	}

  /**
   * Returns all the graphs that hold information which is valid at the given point in time.
   * 
   * @param ti The time instant.
   * 
   * @return A collection of graph names.
   */
	public Collection<String> getGraphsForInstant(TimeInstant ti) {
		Set<String> graphs = new HashSet<String>();

		for(Entry<String, TimeSpan> entry : lookup.entrySet()) { 
			if(entry.getValue().contains(ti)) {
				graphs.add(entry.getKey());
			} else { 
			  //System.err.println("Ignoring " + entry.getKey());			  
			}
		}
		
		return graphs; 
	}

  /**
   * Returns all the graphs that hold information which is valid over a given time span.
   * 
   * @param ts The time span.
   * 
   * @return A collection of graph names.
   */
	public TimeSpan getTimeSpanFor(String graphURI) {
		return lookup.get(graphURI);
	}

}
