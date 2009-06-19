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

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Random;
import java.util.UUID;

import org.custommonkey.xmlunit.XMLAssert;
import org.oucs.gaboto.GabotoConfiguration;
import org.oucs.gaboto.GabotoLibrary;
import org.oucs.gaboto.helperscripts.importing.TEIImporter;
import org.oucs.gaboto.model.Gaboto;
import org.oucs.gaboto.model.GabotoFactory;
import org.oucs.gaboto.timedim.TimeInstant;
import org.oucs.gaboto.timedim.TimeSpan;

public final class Utils {

  public static TimeSpan getRandomTimespan() {
    TimeInstant t1 = getRandomTimeinstant();
    TimeInstant t2 = getRandomTimeinstant();
    if (t1.compareTo(t2) == -1)
      return TimeSpan.createFromInstants(t1, t2);
    else 
      return TimeSpan.createFromInstants(t2, t1);
      
//    return getRandomTimespan(0.5, 0.5, 0.85, 0.95, 0.5, 0.5);
  }

  /**
   * 
   * @return a random timespan
   */
  public static TimeSpan getRandomTimespan(double prob1, double prob2,
      double prob3, double prob4, double prob5, double prob6) {
    Random r = new Random();
    TimeSpan timeSpan = new TimeSpan();
    timeSpan.setStartYear(r.nextInt(2009));
    boolean month = false, day = false;
    if (r.nextDouble() < prob1) {
      month = true;
      timeSpan.setStartMonth(r.nextInt(12));
      if (r.nextDouble() < prob2) {
        day = true;
        timeSpan.setStartDay(r.nextInt(28));
      }
    }

    if (r.nextDouble() < prob3) {
      if (r.nextDouble() < prob4)
        timeSpan.setDurationYear(r.nextInt(2009 - timeSpan.getStartYear()));
      if (month && r.nextDouble() < prob5)
        timeSpan.setDurationMonth(r.nextInt(12));
      if (day && r.nextDouble() < prob6)
        timeSpan.setDurationDay(r.nextInt(30));
    }

    return timeSpan;
  }

  /**
   * 
   * @return a random time instant
   */
  public static TimeInstant getRandomTimeinstant() {
    Random r = new Random();
    TimeInstant ti = new TimeInstant();
    ti.setStartYear(r.nextInt(2009));
    if (r.nextDouble() < 0.5) {
      ti.setStartMonth(r.nextInt(12));
      if (r.nextDouble() < 0.5) {
        ti.setStartDay(r.nextInt(28));
      }
    }

    return ti;
  }

  public static String generateRandomURI() {
    String uri = GabotoLibrary.getConfig().getNSData();

    uri += UUID.randomUUID().toString().substring(0, 10);

    return uri;
  }
  public static String referenceOutputDir = "src/test/reference";
  public static String actualOutputDir = "target";
  public static String filename = "src/test/data/oxpoints_plus.xml"; 


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
    File file = new File(filename);
    if(! file.exists())
      throw new RuntimeException ("Cannot open file " + filename);
    
    GabotoLibrary.init(GabotoConfiguration.fromConfigFile());
    Gaboto oxp = GabotoFactory.getEmptyInMemoryGaboto();
    //oxp = GabotoFactory.getInMemoryGaboto();
    new TEIImporter(oxp, file).run();
    return oxp;
  }
  /*
  protected void assertPageJsonEqual(String actual, String referenceFileName) throws Exception { 
    JSONObject actualJson = JSONObject.fromObject(tidy(actual));
    File generatedFile = new File(actualOutputDir, referenceFileName);
    FileOutputStream generatedOutput = new FileOutputStream(generatedFile);
    generatedOutput.write(actual.getBytes());
    generatedOutput.close();
    
    File referenceFile = new File(referenceOutputDir, referenceFileName);
    if (referenceFile.exists() && ! generateReferenceCopy()) {
      FileInputStream expectedFileinputStream = new FileInputStream (referenceFile);
      byte[] b = new byte[expectedFileinputStream.available()];
      expectedFileinputStream.read(b);
      expectedFileinputStream.close ();
      String cached = new String(b);
      JSONObject expectedJson = JSONObject.fromObject(tidy(cached));
      JSONAssert.assertEquals(expectedJson, actualJson);
    } else { 
      generatedFile.renameTo(referenceFile);
      fail("Reference output file generated: " + referenceFile.getCanonicalPath() + " modify generateCached and rerun");
    }
  }
  */
  public static String tidy(String json) { 
    String out = json.trim();
    if (out.startsWith("[")) { 
      out = out.substring(1);
      if (out.endsWith("]")) 
        out = out.substring(0, out.length() -1);
      else
        throw new RuntimeException("Unbalanced square brackets:" + json);
    }
    return out;
  }
  

}
