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
package net.sf.gaboto.entities.test;

import net.sf.gaboto.test.Utils;

import org.junit.BeforeClass;
import org.junit.Test;
import org.oucs.gaboto.GabotoConfiguration;
import org.oucs.gaboto.GabotoFactory;
import org.oucs.gaboto.entities.GabotoEntity;
import org.oucs.gaboto.entities.pool.GabotoEntityPool;
import org.oucs.gaboto.entities.pool.GabotoEntityPoolConfiguration;
import org.oucs.gaboto.model.Gaboto;
import org.oucs.gaboto.model.GabotoSnapshot;
import org.oucs.gaboto.timedim.TimeInstant;
import org.oucs.gaboto.vocabulary.OxPointsVocab;

import uk.ac.ox.oucs.oxpoints.gaboto.entities.College;
import uk.ac.ox.oucs.oxpoints.gaboto.entities.Website;
import static org.junit.Assert.assertTrue;

public class TestPassiveProperties  {


	@BeforeClass
	public static void setUp() throws Exception {
		GabotoFactory.init(GabotoConfiguration.fromConfigFile());
	}
	
	@Test
	public void testClassicPropertyLoading() {
    Gaboto oxp = Utils.getOxpointsFromXML();
		
		GabotoSnapshot snapshot = oxp.getSnapshot(TimeInstant.now());
		GabotoEntityPoolConfiguration config = new GabotoEntityPoolConfiguration(snapshot);
		config.addAcceptedType(OxPointsVocab.Website_URI);
		GabotoEntityPool pool = GabotoEntityPool.createFrom(config);
		
		boolean foundPassive = false;
    for(GabotoEntity e : pool.getEntities()){
			Website web = (Website) e;
			
			if(web.getIsHomepageIn() != null)
				foundPassive = true;
		}
		
		assertTrue(foundPassive);
	}
	
	@Test
	public void testClassicPropertyLoading2() {
    Gaboto oxp = Utils.getOxpointsFromXML();
		
		GabotoSnapshot snapshot = oxp.getSnapshot(TimeInstant.now());
		GabotoEntityPoolConfiguration config = new GabotoEntityPoolConfiguration(snapshot);
		config.addAcceptedType(OxPointsVocab.College_URI);
		GabotoEntityPool pool = GabotoEntityPool.createFrom(config);
		
		boolean foundPassive = false;
		for(GabotoEntity e : pool.getEntities()){
			College col = (College) e;
			
			if(col.getOccupiedBuildings() != null)
				foundPassive = true;
		}
		
		assertTrue(foundPassive);
	}

}

