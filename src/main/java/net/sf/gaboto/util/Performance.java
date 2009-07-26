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

import java.util.Stack;

/**
 * Simple utility class to do performance testing.
 * 
 * @author Arno Mittelbach
 * @version 0.1
 */
@SuppressWarnings("boxing")
public class Performance {
	
	private static Stack<Double> time = new Stack<Double>();
	
	private static Stack<String> names = new Stack<String>();
	
	/**
	 * Start a new section with the given name
	 * 
	 * @param name The section's name
	 */
	public static void start(String name){
		time.push(new Double(System.currentTimeMillis()));
		names.push(name);
	}
	
	/**
	 * Stop the current section and print the information to System.out.
	 */
  public static void stop(){
		Double t = System.currentTimeMillis() - time.pop();
		String n = names.pop();
		
		System.out.println(n + " --- " + t + "ms");
	}
	
	/**
	 * Stops the current section and returns an Object with the information.
	 * @return Object{String SectionName, Double TimeInMillisecs}
	 */
	public static Object[] stopQ(){
		Double t = System.currentTimeMillis() - time.pop();
		String n = names.pop();
		
		return new Object[]{n,t};
	}
}
