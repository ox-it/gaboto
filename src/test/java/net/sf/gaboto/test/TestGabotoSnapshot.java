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
package net.sf.gaboto.test;

import net.sf.gaboto.test.Utils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.oucs.gaboto.GabotoConfiguration;
import org.oucs.gaboto.GabotoLibrary;
import org.oucs.gaboto.entities.GabotoEntity;
import org.oucs.gaboto.entities.pool.GabotoEntityPool;
import org.oucs.gaboto.entities.pool.GabotoEntityPoolConfiguration;
import org.oucs.gaboto.exceptions.EntityPoolInvalidConfigurationException;
import org.oucs.gaboto.model.Gaboto;
import org.oucs.gaboto.model.GabotoFactory;
import org.oucs.gaboto.model.GabotoSnapshot;
import org.oucs.gaboto.model.QuerySolutionProcessorImpl;
import org.oucs.gaboto.timedim.TimeInstant;
import org.oucs.gaboto.util.GabotoPredefinedQueries;
import org.oucs.gaboto.vocabulary.OxPointsVocab;

import uk.ac.ox.oucs.oxpoints.gaboto.entities.College;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DC_11;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class TestGabotoSnapshot {


	@BeforeClass
  public static void setUp() throws Exception {
    GabotoLibrary.init(GabotoConfiguration.fromConfigFile());
  }
	@AfterClass
  public static void tearDown() throws Exception {
	  // FIXME I do not understand the need for this
	  GabotoFactory.clear();
  }
	
	@Test
	public void testSPARQLSelect() throws EntityPoolInvalidConfigurationException {
    Gaboto oxp = Utils.getOxpointsFromXML();
		
		GabotoSnapshot nowSnap = oxp.getSnapshot(TimeInstant.now());
		GabotoEntityPoolConfiguration config = new GabotoEntityPoolConfiguration(nowSnap);
		config.addAcceptedType(OxPointsVocab.College_URI);
		config.setAddReferencedEntitiesToPool(false);
	
		GabotoEntityPool pool = GabotoEntityPool.createFrom(config);
		
		
		
		for(GabotoEntity e : pool.getEntities()){
			College col = (College) e;
			final String name = col.getName();
			final String uri = col.getUri();
			
			String query = GabotoPredefinedQueries.getStandardPrefixes();
			query += "SELECT ?name WHERE { <" + uri + ">  dc:title  ?name . }";
			
			nowSnap.execSPARQLSelect(query, new QuerySolutionProcessorImpl(){

				private int i = 1;
				
				public void processSolution(QuerySolution solution) {
					assertEquals(1, i);
					i++;
					assertEquals(name, solution.getLiteral("name").getValue());
				}
				
			});
		}
	}
	
	@Test
	public void testSPARQLAsk() throws EntityPoolInvalidConfigurationException {
    Gaboto oxp = Utils.getOxpointsFromXML();
		
		GabotoSnapshot nowSnap = oxp.getSnapshot(TimeInstant.now());
		
		GabotoEntityPoolConfiguration config = new GabotoEntityPoolConfiguration(nowSnap);
		config.addAcceptedType(OxPointsVocab.College_URI);
		config.setAddReferencedEntitiesToPool(false);
	
		GabotoEntityPool pool = GabotoEntityPool.createFrom(config);
		
		for(GabotoEntity e : pool.getEntities()){
			College col = (College) e;
			final String name = col.getName();
			final String uri = col.getUri();
			
			String query = GabotoPredefinedQueries.getStandardPrefixes();
			query += "ASK { <" + uri + ">  dc:title  \"" + name + "\" . }";
			assertTrue(nowSnap.execSPARQLAsk(query));
		}
	}
	
	@Test
	public void testSPARQLDescribe() throws EntityPoolInvalidConfigurationException {
    Gaboto oxp = Utils.getOxpointsFromXML();
		
		GabotoSnapshot nowSnap = oxp.getSnapshot(TimeInstant.now());
		GabotoEntityPoolConfiguration config = new GabotoEntityPoolConfiguration(nowSnap);
		config.addAcceptedType(OxPointsVocab.College_URI);
	
		GabotoEntityPool pool = GabotoEntityPool.createFrom(config);
    assertTrue("it is " + pool.getSize(), pool.getSize() != 0);
		GabotoSnapshot collegeSnap = pool.createSnapshot();
		
		
		String query = GabotoPredefinedQueries.getStandardPrefixes();
		query += "PREFIX oxp: <" + OxPointsVocab.NS + ">\n"; 
		query += "DESCRIBE ?x WHERE { ?x rdf:type oxp:College }";
		GabotoSnapshot describedSnap = nowSnap.execSPARQLDescribe(query);
		
		
		Model m1 = describedSnap.getModel();
		Model m2 = collegeSnap.getModel();
		StmtIterator it = m1.listStatements();
		while(it.hasNext()){
			Statement stmt = it.nextStatement();
			if(! m2.contains(stmt))
				System.out.println(stmt);
		}
		
		assertEquals(describedSnap.size(),collegeSnap.size());
	}
	
	@Test
	public void testLoadEntities(){
    Gaboto oxp = Utils.getOxpointsFromXML();
		
		GabotoSnapshot nowSnap = oxp.getSnapshot(TimeInstant.now());
		GabotoEntityPool pool = nowSnap.loadEntitiesWithProperty(DC_11.title, "Somerville College");
    assertTrue("it is " + pool.getSize(), pool.getSize() == 1);
	}
}
