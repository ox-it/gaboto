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
package org.oucs.gaboto;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.xml.sax.SAXException;

/**
 * Needed to initialize gaboto. 
 * 
 * @author Arno Mittelbach
 *
 */
public class GabotoLibrary {

	private static GabotoConfiguration config;
	
	private static Logger logger = Logger.getLogger(GabotoLibrary.class.getName());
	
	/**
	 * Initializes the Gaboto system (this has to be done before Gaboto can be used).
	 * @param config
	 */
	public static void init(GabotoConfiguration config){
		GabotoLibrary.config = config;
		
		// configure logging
		PropertyConfigurator.configure("log4j.properties");
		
		logger.info("Gaboto system has been initialized.");
	}

	/**
	 * Returns the Gaboto configuration.
	 * @return The Gaboto configuration.
	 */
	public static GabotoConfiguration getConfig(){
		if(config == null)
			throw new IllegalStateException("Gaboto has not yet been configured.");
		
		return config;
	}
	
	/**
	 * 
	 * @param config
	 * @param entityOutputDir
	 * @param beanOutputDir
	 * @param miscOutputDir
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static void generateClassesFromConfig(File config, File entityOutputDir, File beanOutputDir, File miscOutputDir) throws ParserConfigurationException, SAXException, IOException{
		new GabotoClassGeneration(config, entityOutputDir, beanOutputDir, miscOutputDir).run();
	}
}
