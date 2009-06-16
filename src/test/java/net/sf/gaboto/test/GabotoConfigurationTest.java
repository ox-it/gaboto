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

package net.sf.gaboto.test;

import org.oucs.gaboto.GabotoConfiguration;

import junit.framework.TestCase;

/**
 * @since 9 Jun 2009
 *
 */
public class GabotoConfigurationTest extends TestCase {

  /**
   * @param name
   */
  public GabotoConfigurationTest(String name) {
    super(name);
  }

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * Test method for {@link org.oucs.gaboto.GabotoConfiguration#fromConfigFile()}.
   */
  public void testFromConfigFile() throws Exception {
    GabotoConfiguration gc = GabotoConfiguration.fromConfigFile();
    //assertEquals("org.postgresql.Driver", gc.getDbDriver());
    //assertEquals("org.hsqldb.jdbcDriver", gc.getDbDriver());
  }

  /**
   * Test method for {@link org.oucs.gaboto.GabotoConfiguration#fromConfigFile(java.lang.String)}.
   */
  public void testFromConfigFileString() {
  }

  /**
   * Test method for {@link org.oucs.gaboto.GabotoConfiguration#getDbURL()}.
   */
  public void testGetDbURL() {
  }

  /**
   * Test method for {@link org.oucs.gaboto.GabotoConfiguration#setDbURL(java.lang.String)}.
   */
  public void testSetDbURL() {
  }

  /**
   * Test method for {@link org.oucs.gaboto.GabotoConfiguration#getDbUser()}.
   */
  public void testGetDbUser() {
    
  }

  /**
   * Test method for {@link org.oucs.gaboto.GabotoConfiguration#setDbUser(java.lang.String)}.
   */
  public void testSetDbUser() {
    
  }

  /**
   * Test method for {@link org.oucs.gaboto.GabotoConfiguration#getDbPassword()}.
   */
  public void testGetDbPassword() {
    
  }

  /**
   * Test method for {@link org.oucs.gaboto.GabotoConfiguration#setDbPassword(java.lang.String)}.
   */
  public void testSetDbPassword() {
    
  }

  /**
   * Test method for {@link org.oucs.gaboto.GabotoConfiguration#getDbEngineName()}.
   */
  public void testGetDbEngineName() {
  }

  /**
   * Test method for {@link org.oucs.gaboto.GabotoConfiguration#setDbEngineName(java.lang.String)}.
   */
  public void testSetDbEngineName() {
    
  }

  /**
   * Test method for {@link org.oucs.gaboto.GabotoConfiguration#getDbDriver()}.
   */
  public void testGetDbDriver() {
    
  }

  /**
   * Test method for {@link org.oucs.gaboto.GabotoConfiguration#setDbDriver(java.lang.String)}.
   */
  public void testSetDbDriver() {
    
  }

  /**
   * Test method for {@link org.oucs.gaboto.GabotoConfiguration#getNSGraphs()}.
   */
  public void testGetNSGraphs() {
    
  }

  /**
   * Test method for {@link org.oucs.gaboto.GabotoConfiguration#setNSGraphs(java.lang.String)}.
   */
  public void testSetNSGraphs() {
    
  }

  /**
   * Test method for {@link org.oucs.gaboto.GabotoConfiguration#getNSData()}.
   */
  public void testGetNSData() {
    
  }

  /**
   * Test method for {@link org.oucs.gaboto.GabotoConfiguration#setNSData(java.lang.String)}.
   */
  public void testSetNSData() {
    
  }

  /**
   * Test method for {@link org.oucs.gaboto.GabotoConfiguration#getGKG()}.
   */
  public void testGetGKG() {
    
  }

  /**
   * Test method for {@link org.oucs.gaboto.GabotoConfiguration#getCDG()}.
   */
  public void testGetCDG() {
    
  }

  /**
   * Test method for {@link org.oucs.gaboto.GabotoConfiguration#getDefaultGraph()}.
   */
  public void testGetDefaultGraph() {
    
  }

  /**
   * Test method for {@link org.oucs.gaboto.GabotoConfiguration#getNamespacePrefixes()}.
   */
  public void testGetNamespacePrefixes() {
    
  }

  /**
   * Test method for {@link org.oucs.gaboto.GabotoConfiguration#setNamespacePrefixes(java.util.Map)}.
   */
  public void testSetNamespacePrefixes() {
    
  }

}
