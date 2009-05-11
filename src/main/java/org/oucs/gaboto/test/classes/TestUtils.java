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
package org.oucs.gaboto.test.classes;

import java.util.Random;
import java.util.UUID;

import org.oucs.gaboto.GabotoLibrary;
import org.oucs.gaboto.timedim.TimeInstant;
import org.oucs.gaboto.timedim.TimeSpan;

public final class TestUtils {

	
	public static TimeSpan getRandomTimespan(){
		return getRandomTimespan(0.5, 0.5, 0.85, 0.95, 0.5, 0.5);
	}
	
	/**
	 *
	 * @return
	 */
	public static TimeSpan getRandomTimespan(double prob1, double prob2, double prob3, double prob4, double prob5, double prob6){
		Random r = new Random();
		TimeSpan ts = new TimeSpan();
		ts.setStartYear(r.nextInt(2009));
		boolean month = false, day = false;
		if(r.nextDouble() < prob1){
			month = true;
			ts.setStartMonth(r.nextInt(12));
			if(r.nextDouble() < prob2){
				day = true;
				ts.setStartDay(r.nextInt(28));
			}
		}
		
		if(r.nextDouble() < prob3){
			if(r.nextDouble() < prob4)
				ts.setDurationYear(r.nextInt(2009-ts.getStartYear()));
			if(month && r.nextDouble() < prob5)
				ts.setDurationMonth(r.nextInt(12));
			if(day && r.nextDouble() < prob6)
				ts.setDurationDay(r.nextInt(30));
		}
		
		return ts;
	}
	
	/**
	 *
	 * @return
	 */
	public static TimeInstant getRandomTimeinstant(){
		Random r = new Random();
		TimeInstant ti = new TimeInstant();
		ti.setStartYear(r.nextInt(2009));
		if(r.nextDouble() < 0.5){
			ti.setStartMonth(r.nextInt(12));
			if(r.nextDouble() < 0.5){
				ti.setStartDay(r.nextInt(28));
			}
		}
		
		return ti;
	}
	
	public static String generateRandomURI(){
		String uri = GabotoLibrary.getConfig().getNSData();
		
		uri += UUID.randomUUID().toString().substring(0,10);
		
		return uri;
	}
	

}
