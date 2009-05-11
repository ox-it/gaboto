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
package net.sf.gaboto.test.performance;

import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.oucs.gaboto.GabotoConfiguration;
import org.oucs.gaboto.GabotoLibrary;
import org.oucs.gaboto.entities.pool.GabotoEntityPool;
import org.oucs.gaboto.entities.pool.GabotoEntityPoolConfiguration;
import org.oucs.gaboto.exceptions.EntityPoolInvalidConfigurationException;
import org.oucs.gaboto.model.Gaboto;
import org.oucs.gaboto.model.GabotoFactory;
import org.oucs.gaboto.model.GabotoSnapshot;
import org.oucs.gaboto.model.query.GabotoQuery;
import org.oucs.gaboto.sample.CollegesNearEntity;
import org.oucs.gaboto.timedim.TimeInstant;
import org.oucs.gaboto.util.PerformanceAverager;

public class testPoolCreationPerformance {

	private static int RUNS = 50;

	@BeforeClass
	public static void setUp() throws Exception {
		GabotoLibrary.init(GabotoConfiguration.fromConfigFile());
	}
	
	@Test
	public void testSimplePoolCreation() throws EntityPoolInvalidConfigurationException{
		Gaboto oxp = GabotoFactory.getInMemoryGaboto();

		PerformanceAverager perf = new PerformanceAverager("Simple Pool Creation");
		for(int i = 0; i < RUNS; i++){
			perf.start("creation");
			
			GabotoSnapshot snap = oxp.getSnapshot(TimeInstant.now());
			GabotoEntityPoolConfiguration config = new GabotoEntityPoolConfiguration(snap);
			GabotoEntityPool.createFrom(config);
			
			perf.stop();
		}
		
		System.out.println(perf);
	}
	
	@Test
	public void testSimplePoolCreationWithAllPassive() throws EntityPoolInvalidConfigurationException{
		Gaboto oxp = GabotoFactory.getInMemoryGaboto();

		PerformanceAverager perf = new PerformanceAverager("Simple Pool Creation with passive creation");
		for(int i = 0; i < RUNS; i++){
			perf.start("creation");
			
			GabotoSnapshot snap = oxp.getSnapshot(TimeInstant.now());
			GabotoEntityPoolConfiguration config = new GabotoEntityPoolConfiguration(snap);
			config.setCreatePassiveEntities(true);
			GabotoEntityPool.createFrom(config);
			
			perf.stop();
		}
		
		System.out.println(perf);
	}
	
	
	// FIXME Broken - no DB configured and will not run from rdf 
	//@Test
	public void brokenTestQuery1() throws Exception{
	   // load Gaboto
    GabotoLibrary.init(GabotoConfiguration.fromConfigFile());
    Gaboto gaboto = GabotoFactory.getEmptyInMemoryGaboto();
    
    gaboto.read(getResourceOrDie("graphs.rdf"), getResourceOrDie("cdg.rdf"));
 
    gaboto = GabotoFactory.getInMemoryGaboto();

    gaboto.recreateTimeDimensionIndex();
    
		CollegesNearEntity query = new CollegesNearEntity(gaboto, "Somerville College", 10, TimeInstant.now());
		query.prepare();
		
		PerformanceAverager perf = new PerformanceAverager("Test query 1");
		for(int i = 0; i < RUNS; i++){
			perf.start("creation");
			
			query.execute(GabotoQuery.FORMAT_ENTITY_POOL);
			
			perf.stop();
		}
		
		System.out.println(perf);
	}
  private InputStream getResourceOrDie(String fileName) { 
    String resourceName = "exampledata/" + fileName;
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
    if (is == null) 
      throw new NullPointerException("File " + resourceName + " cannot be loaded");
    return is;
  }
	
}
