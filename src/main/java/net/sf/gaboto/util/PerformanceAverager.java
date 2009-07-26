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
package net.sf.gaboto.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class PerformanceAverager {

	private Map<String, Double> lookup = new HashMap<String, Double>();
	private String title;
	
	public PerformanceAverager(String title){
		this.title = title;
	}
	
	public void start(String name){
		Performance.start(name);
	}
	
	public void stop(){
		Object[] result = Performance.stopQ();
		if(! lookup.containsKey(result[0]))
			lookup.put((String)result[0], new Double(0));
		
		Double avg = lookup.get(result[0]) + (Double)result[1];
		avg = avg/2;
		lookup.put((String)result[0], avg);
	}
	
	@Override
	public String toString(){
		String result = "Performance results (" + title + "):\n";
		for(Entry<String, Double> e : lookup.entrySet())
			result += e.getKey() + ":\t\t" + e.getValue() + "ms\n";
		
		return result;
	}
}
