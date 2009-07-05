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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.oucs.gaboto.model.GabotoOntologyLookup;
import org.oucs.gaboto.exceptions.GabotoRuntimeException;
import org.oucs.gaboto.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A configuration object for Gaboto.
 * 
 * 
 * @author Arno Mittelbach
 *
 */
public class GabotoConfiguration {

  private String dbURL;
	private String dbUser;
	private String dbPassword;
	private String dbEngineName;
	private String dbDriver;
	
	private String NSGraphs = "http://gaboto.sf.net/graphs/";
	private String NSData   = "http://gaboto.sf.net/data/";
	
	private Map<String, String> namespacePrefixes = new HashMap<String, String>();
	
	private GabotoOntologyLookup lookup;
	
	public static GabotoConfiguration fromConfigFile() {
		try {
      return fromConfigFile("Gaboto.xml");
    } catch (Exception e) {
      throw new GabotoRuntimeException(e);
    }
	}
	
	public static GabotoConfiguration fromConfigFile(String name) 
	    throws ParserConfigurationException, SAXException, IOException{
		GabotoConfiguration config = new GabotoConfiguration();
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
		Document doc = XMLUtils.readInputStreamIntoJAXPDoc(is);
		
		//
		Element configEl = (Element) doc.getElementsByTagName("config").item(0);
		// database
		NodeList configChildren = configEl.getChildNodes();
		for(int i = 0; i < configChildren.getLength(); i++){
			if(! (configChildren.item(i) instanceof Element))
				continue;
			Element configSection = (Element) configChildren.item(i);
			
			if(configSection.getNodeName().equals("database")){
				NodeList databaseChildren = configSection.getChildNodes();
				for(int j = 0; j < databaseChildren.getLength(); j++){
					if(! (databaseChildren.item(j) instanceof Element))
						continue;
					Element databaseProp = (Element) databaseChildren.item(j);
					
					if(databaseProp.getNodeName().equals("engineName")){
						config.dbEngineName = databaseProp.getTextContent();
					} else if(databaseProp.getNodeName().equals("url")){
            config.dbURL = databaseProp.getTextContent();
          } else if(databaseProp.getNodeName().equals("user")){
						config.dbUser = databaseProp.getTextContent();
					} else if(databaseProp.getNodeName().equals("password")){
						config.dbPassword = databaseProp.getTextContent();
					} else if(databaseProp.getNodeName().equals("driver")){
						config.dbDriver = databaseProp.getTextContent();
					}
				}
      } else if(configSection.getNodeName().equals("namespaces")){
        config.NSData = configSection.getAttribute("data");
        config.NSGraphs = configSection.getAttribute("graphs");
      } else if(configSection.getNodeName().equals("namespacePrefixes")){
				NodeList nspChildren = configSection.getChildNodes();
				for(int j = 0; j < nspChildren.getLength(); j++){
					if(! (nspChildren.item(j) instanceof Element))
						continue;
					Element namespacePrefix = (Element) nspChildren.item(j);
					if(! namespacePrefix.getNodeName().equals("namespacePrefix"))
						continue;
					config.namespacePrefixes.put(namespacePrefix.getAttribute("prefix"), namespacePrefix.getAttribute("ns"));
				}
      } else if(configSection.getNodeName().equals("lookupClass")){
        String lookupName =  configSection.getAttribute("class");
        Class<?> clazz;
        try {
          clazz = Class.forName(lookupName);
        } catch (ClassNotFoundException e) {
          throw new GabotoRuntimeException("GabotoOntologyLookup not found " + lookupName, e);
        }
        Object object;
        try {
          object = clazz.newInstance();
        } catch (Exception e) {
          throw new GabotoRuntimeException(e);
        }
        if (!(object instanceof GabotoOntologyLookup))
          throw new GabotoRuntimeException("GabotoOntologyLookup is not of correct type " + lookupName);
        config.lookup = (GabotoOntologyLookup)object;
        } else throw new GabotoRuntimeException("Unrecognised configuration section " + configSection.getNodeName());
		}
		
		return config;
	}
	
	
	
	
	public String getDbURL() {
		return dbURL;
	}

	public void setDbURL(String dbURL) {
		this.dbURL = dbURL;
	}

	public String getDbUser() {
		return dbUser;
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public String getDbEngineName() {
		return dbEngineName;
	}

	public void setDbEngineName(String dbEngineName) {
		this.dbEngineName = dbEngineName;
	}

	public String getDbDriver() {
		return dbDriver;
	}

	public void setDbDriver(String dbDriver) {
		this.dbDriver = dbDriver;
	}

	public String getNSGraphs() {
		return NSGraphs;
	}

	public void setNSGraphs(String graphs) {
		NSGraphs = graphs;
	}

	public String getNSData() {
		return NSData;
	}

	public void setNSData(String data) {
		NSData = data;
	}

	public String getGKG(){
		return NSGraphs + "gkg.rdf";
	}
	
	public String getCDG(){
		return NSGraphs + "cdg.rdf";
	}
	
	public String getDefaultGraph(){
		return NSGraphs + "default.rdf";
	}

	public Map<String, String> getNamespacePrefixes() {
		return namespacePrefixes;
	}

	public void setNamespacePrefixes(Map<String, String> namespacePrefixes) {
		this.namespacePrefixes = namespacePrefixes;
	}

  public GabotoOntologyLookup getGabotoOntologyLookup() {
    return lookup;
  }
	
	
}
