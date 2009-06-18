/**
 * Copyright 2009 University of Oxford
 *
 * Written by Tim Pizey for the Erewhon Project
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
/**
 * 
 */
package net.sf.gaboto.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.custommonkey.xmlunit.XMLAssert;
import org.oucs.gaboto.GabotoConfiguration;
import org.oucs.gaboto.GabotoLibrary;
import org.oucs.gaboto.helperscripts.importing.TEIImporter;
import org.oucs.gaboto.model.Gaboto;
import org.oucs.gaboto.model.GabotoFactory;

import junit.framework.TestCase;

/**
 * @author Tim Pizey
 * @since 15 May 2009
 *
 */
public class GabotoTestCase extends TestCase {
  
  protected static String referenceOutputDir = "src/test/reference";
  protected static String actualOutputDir = "target";
  static String filename = "src/test/data/oxpoints_plus.xml"; 

  /**
   * Default constructor.
   */
  public GabotoTestCase() {
    super();
  }

  /**
   * Constructor, with name.
   * @param name
   */
  public GabotoTestCase(String name) {
    super(name);
  }

  protected boolean generateReferenceCopy() { 
    return false;
  }
  protected void assertXmlEqual(String actual, String fileName) throws Exception { 
    File actualFile = new File(actualOutputDir, fileName);
    FileOutputStream actualOutputStream = new FileOutputStream(actualFile);
    actualOutputStream.write(actual.getBytes());
    actualOutputStream.close();
    
    File referenceFile = new File(referenceOutputDir, fileName);
    if (referenceFile.exists() && ! generateReferenceCopy()) {
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

  protected Gaboto getOxpointsFromXML() { 
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
  protected static String tidy(String json) { 
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
