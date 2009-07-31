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

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import net.sf.gaboto.Gaboto;
import net.sf.gaboto.GabotoConfiguration;
import net.sf.gaboto.GabotoFactory;

import org.custommonkey.xmlunit.XMLAssert;

import uk.ac.ox.oucs.oxpoints.gaboto.TEIImporter;

public final class Utils {

  
  public static String referenceOutputDir = "src/test/reference";
  public static String actualOutputDir = "target";
  public static String filename = "src/test/data/oxpoints_plus.xml"; 

  public static void assertGeneratedFileContentsStringEqual(String canonicalFilename) throws Exception {
    File actualFile = new File(actualOutputDir, canonicalFilename);
    FileInputStream actualFileInputStream = new FileInputStream(actualFile);
    byte[] a = new byte[actualFileInputStream.available()];
    actualFileInputStream.read(a);
    actualFileInputStream.close();
    String actual = new String(a);
    
    File referenceFile = new File(referenceOutputDir, canonicalFilename);
    FileInputStream referenceFileInputStream = new FileInputStream(referenceFile);
    byte[] b = new byte[referenceFileInputStream.available()];
    referenceFileInputStream.read(b);
    referenceFileInputStream.close();
    String cached = new String(b);
    assertEquals("Cached not equal to generated", cached, actual);
  }
  public static void assertFileContentsStringEqual(String filename1, String filename2) throws Exception {
    File actualFile = new File(actualOutputDir, filename1);
    FileInputStream actualFileInputStream = new FileInputStream(actualFile);
    byte[] a = new byte[actualFileInputStream.available()];
    actualFileInputStream.read(a);
    actualFileInputStream.close();
    String actual = new String(a);
    
    File referenceFile = new File(actualOutputDir, filename2);
    FileInputStream referenceFileInputStream = new FileInputStream(referenceFile);
    byte[] b = new byte[referenceFileInputStream.available()];
    referenceFileInputStream.read(b);
    referenceFileInputStream.close();
    String cached = new String(b);
    assertEquals("Cached not equal to generated", cached, actual);
  }

  public static void assertXmlEqual(String actual, String fileName) throws Exception { 
    File actualFile = new File(actualOutputDir, fileName);
    FileOutputStream actualOutputStream = new FileOutputStream(actualFile);
    actualOutputStream.write(actual.getBytes());
    actualOutputStream.close();
    
    File referenceFile = new File(referenceOutputDir, fileName);
    if (referenceFile.exists()) {
      FileInputStream file = new FileInputStream (referenceFile);
      byte[] b = new byte[file.available()];
      file.read(b);
      file.close ();
      String cached = new String(b);
      XMLAssert.assertXMLEqual("Cached not equal to generated", cached, actual);
    } else { 
      actualFile.renameTo(referenceFile);
      fail("Reference output file generated: " + referenceFile.getCanonicalPath() + " modify generateCached and rerun");
    }
  }
  public static Gaboto getOxpointsFromXML() {
    return getOxpointsFromXML(filename);
  }
  public static Gaboto getOxpointsFromXML(String filenameIn) { 
    System.err.println("Reading oxp from " + filenameIn);
    File file = new File(filenameIn);
    if(! file.exists())
      throw new RuntimeException ("Cannot open file " + filenameIn);
    
    GabotoFactory.init(GabotoConfiguration.fromConfigFile());
    Gaboto oxp = GabotoFactory.getEmptyInMemoryGaboto();
    //oxp = GabotoFactory.getInMemoryGaboto();
    new TEIImporter(oxp, file).run();
    return oxp;
    
  }
}
