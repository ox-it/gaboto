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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;

import org.junit.BeforeClass;
import org.junit.Test;
import org.oucs.gaboto.GabotoConfiguration;
import org.oucs.gaboto.GabotoLibrary;
import org.oucs.gaboto.beans.Location;
import org.oucs.gaboto.entities.Building;
import org.oucs.gaboto.entities.Carpark;
import org.oucs.gaboto.entities.Unit;
import org.oucs.gaboto.entities.pool.GabotoEntityPool;
import org.oucs.gaboto.exceptions.EntityAlreadyExistsException;
import org.oucs.gaboto.model.Gaboto;
import org.oucs.gaboto.model.GabotoFactory;
import org.oucs.gaboto.model.GabotoSnapshot;
import org.oucs.gaboto.model.QuerySolutionProcessor;
import org.oucs.gaboto.timedim.TimeInstant;
import org.oucs.gaboto.timedim.TimeSpan;
import org.oucs.gaboto.util.GabotoPredefinedQueries;
import org.oucs.gaboto.vocabulary.OxPointsVocab;

import com.hp.hpl.jena.query.QuerySolution;

public class TestGabotoEntity {

	@BeforeClass
	public static void setUp() throws Exception {
		GabotoLibrary.init(GabotoConfiguration.fromConfigFile());
	}
	
	@Test
	public void testGetPropertyValue(){
		Gaboto oxp = GabotoFactory.getPersistentGaboto();
		Gaboto oxp_mem = GabotoFactory.getInMemoryGaboto();
		
		Building b = new Building();
		b.setTimeSpan(new TimeSpan(1900,null,null));
		b.setName("Test Building");
		Location loc = new Location();
		loc.setPos("50.12312414234 0.12312432");
		b.setLocation(loc);
		
		Building b2 = new Building();
		b2.setTimeSpan(new TimeSpan(1900,null,null));
		b2.setName("Test Building 2");
		Location loc2 = new Location();
		loc2.setPos("57.1239812 21.123987");
		b2.setLocation(loc2);
		
		Unit u = new Unit();
		u.setUri(oxp.generateID("unit"));
		u.setTimeSpan(new TimeSpan(1900,null,null));
		u.setName("Test Unit");
		u.setPrimaryPlace(b);
		
		Unit u2 = new Unit();
		u2.setUri(oxp.generateID("unit"));
		u2.setTimeSpan(new TimeSpan(1900,null,null));
		u2.setName("Test Unit");
		u2.setPrimaryPlace(b2);
		
		Unit u3 = new Unit();
		u3.setUri(oxp.generateID("unit"));
		u3.setTimeSpan(new TimeSpan(1900,null,null));
		u3.setName("Test Unit");
		u3.setSubsetOf(u2);
		
		
		assertEquals(loc, u.getPropertyValue(OxPointsVocab.hasLocation_URI));
		assertEquals(loc2, u3.getPropertyValue(OxPointsVocab.hasLocation_URI));
	}
		
	
	@Test
	public void testGetPropertyValue2() throws EntityAlreadyExistsException{
		Gaboto oxp = GabotoFactory.getPersistentGaboto();
		Gaboto oxp_mem = GabotoFactory.getInMemoryGaboto();
		
		Building b = new Building();
		b.setUri(oxp.generateID("building"));
		b.setTimeSpan(new TimeSpan(1900,null,null));
		b.setName("Test Building");
		Location loc = new Location();
		loc.setPos("50.12312414234 0.12312432");
		b.setLocation(loc);
		
		Unit u = new Unit();
		u.setUri(oxp.generateID("unit"));
		u.setTimeSpan(new TimeSpan(1900,null,null));
		u.setName("Test Unit");
		u.addOccupiedBuilding(b);
		
		// add to data store
		oxp.add(b);
		oxp.add(u);
		
		// create pool for passive properties
		GabotoEntityPool pool = new GabotoEntityPool(oxp_mem, oxp_mem.getSnapshot(TimeInstant.now()));
		pool.addEntity(b);
		pool.addEntity(u);
		
		assertEquals(loc, u.getPropertyValue(OxPointsVocab.hasLocation_URI));
	}
	
	@Test
	public void testTypedLiteral1() throws EntityAlreadyExistsException{
		Gaboto gaboto = GabotoFactory.getPersistentGaboto();
		Gaboto gaboto_mem = GabotoFactory.getInMemoryGaboto();
		
		String uri1 = gaboto.generateID("carpark");
		Carpark cp1 = new Carpark();
		cp1.setUri(uri1);
		cp1.setName("small carpark");
		cp1.setCapacity(30);
		
		final String uri2 = gaboto.generateID("carpark");
		Carpark cp2 = new Carpark();
		cp2.setUri(uri2);
		cp2.setName("big carpark");
		cp2.setCapacity(80);
		
		// add carparks
		gaboto.add(cp1);
		gaboto.add(cp2);
		
		// snapshot
		GabotoSnapshot snap = gaboto.getSnapshot(TimeInstant.now());

		// ask for small
		String query = GabotoPredefinedQueries.getStandardPrefixes();
		query += "SELECT ?cp WHERE { \n";
		query += "?cp a oxp:Carpark .\n";
		query += "?cp oxp:capacity ?capacity .\n";
		query += "FILTER (?capacity > 50) .\n";
		query += "}";
		
		final Collection<String> foundURIs = new HashSet<String>();
		snap.execSPARQLSelect(query, new QuerySolutionProcessor(){

			public void processSolution(QuerySolution solution) {
				foundURIs.add(solution.getResource("cp").getURI());
			}

			public boolean stopProcessing() {
				return false;
			}
			
		});
		
		assertTrue(foundURIs.contains(uri2));
	}
		
}
