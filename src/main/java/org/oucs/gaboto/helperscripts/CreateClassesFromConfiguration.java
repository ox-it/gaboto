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
package org.oucs.gaboto.helperscripts;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import net.sf.gaboto.generation.GabotoGenerator;

import org.xml.sax.SAXException;

/**
 * Called from Ant with the following arguments: 
           <arg value="src/main/conf/Gaboto.xml"/>
           <arg value="src/main/java/org/oucs/gaboto/entities"/>
           <arg value="src/main/java/org/oucs/gaboto/beans"/>
           <arg value="src/main/java/org/oucs/gaboto/util"/>
 *
 *
 */
public class CreateClassesFromConfiguration {

	

	/**
	 * @param args
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		if(args.length != 4)
			showHelp();
		String filename = args[0];
		File config = new File(filename);
		if(! config.exists() )
			showHelp();
		
		String entityOutputDirName = args[1];
		File entityOutputDir = new File(entityOutputDirName);
		if(! entityOutputDir.exists() || ! entityOutputDir.isDirectory())
			showHelp();

		String beanOutputDirName = args[2];
		File beanOutputDir = new File(beanOutputDirName);
		if(! beanOutputDir.exists() || ! beanOutputDir.isDirectory())
			showHelp();
		
		String miscOutputDirName = args[3];
		File miscOutputDir = new File(miscOutputDirName);
		if(! miscOutputDir.exists() || ! miscOutputDir.isDirectory())
			showHelp();

		generateClassesFromConfig(config,entityOutputDir, beanOutputDir, miscOutputDir);
	}
	

	private static void showHelp() {
		System.out.println("Argument one must be a xml config file and two, three and four need to be a directories.");
		System.exit(1);
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
    new GabotoGenerator(config, entityOutputDir, beanOutputDir, miscOutputDir).run();
  }
	
}
