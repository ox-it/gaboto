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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.oucs.gaboto.model.IncoherenceException;
import org.oucs.gaboto.timedim.TimeInstant;
import org.oucs.gaboto.timedim.TimeSpan;
import org.oucs.gaboto.util.GabotoPredefinedQueries;

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
 * This class does not use any proper data structure to do the task.
 * 
 * TODO think of a proper tree to do the indexing!
 * 
 * @author Arno Mittelbach
 *
 */
public class TimeDimensionIndexerImpl implements TimeDimensionIndexer {

	private Map<String, TimeSpan> lookup = new HashMap<String, TimeSpan>();
	
	public void createIndex(Model cdg) throws IncoherenceException {
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
				if(null != startDayNode)
					ts.setStartDay(((Literal)startDayNode).getInt());
				
				if(null != durationYearNode)
					ts.setDurationYear(((Literal)durationYearNode).getInt());
				if(null != durationMonthNode)
					ts.setDurationMonth(((Literal)durationMonthNode).getInt());
				if(null != durationDayNode)
					ts.setDurationDay(((Literal)durationDayNode).getInt());
				
				add(graph, ts);
			} catch(IllegalArgumentException e){
				throw new IncoherenceException("The data seems to be corrupt. Can not load graph " + graph , e);
			}
		}
		
	}
	
	public void add(String graph, TimeSpan ts){
		lookup.put(graph, ts);
	}
	
	public void add(NamedGraph graph, TimeSpan ts){
		add(graph.getGraphName().getURI(), ts);
	}

	public Collection<String> getGraphsForDuration(TimeSpan ts) {
		Set<String> graphs = new HashSet<String>();

		for(Entry<String, TimeSpan> entry : lookup.entrySet())
			if(entry.getValue().contains(ts))
				graphs.add(entry.getKey());
		
		return graphs; 
	}

	public Collection<String> getGraphsForInstant(TimeInstant ti) {
		Set<String> graphs = new HashSet<String>();

		for(Entry<String, TimeSpan> entry : lookup.entrySet())
			if(entry.getValue().contains(ti))
				graphs.add(entry.getKey());
		
		return graphs; 
	}

	public TimeSpan getTimeSpanFor(String graphURI) {
		return lookup.get(graphURI);
	}

}
