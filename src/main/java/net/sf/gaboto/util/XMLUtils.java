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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.gaboto.GabotoRuntimeException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Provides various helper functions to work with XML.
 * 
 * @author Arno Mittelbach
 * 
 */
public class XMLUtils {

  /**
   * Reads a file into a JAXP XML document.
   * 
   * @param file
   *          The input file.
   * @return The JAXP XML document.
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  public static Document readInputFileIntoJAXPDoc(File file)
      throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
        .newInstance();
    docBuilderFactory.setNamespaceAware(true);
    DocumentBuilder docBuilder;
    Document doc = null;

    docBuilder = docBuilderFactory.newDocumentBuilder();

    doc = docBuilder.parse(file);

    return doc;
  }

  /**
   * Reads an InputStream into a JAXP XML document.
   * 
   * @param in
   *          The input stream.
   * @return The JAXP XML document.
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  public static Document readInputStreamIntoJAXPDoc(InputStream in)
      throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
        .newInstance();
    docBuilderFactory.setNamespaceAware(true);
    DocumentBuilder docBuilder;
    Document doc = null;

    docBuilder = docBuilderFactory.newDocumentBuilder();
    doc = docBuilder.parse(in);

    return doc;
  }

  /**
   * Creates an empty JAXP XML document.
   * 
   * @return An empty JAXP XML document.
   */
  public static Document getNewEmptyJAXPDoc() {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
        .newInstance();
    documentBuilderFactory.setNamespaceAware(true);
    documentBuilderFactory.setCoalescing(false);
    DocumentBuilder documentBuilder;

    try {
      documentBuilder = documentBuilderFactory.newDocumentBuilder();
      return documentBuilder.newDocument();
    } catch (ParserConfigurationException e) {
      new GabotoRuntimeException(e);
    }

    return null;
  }

  public static String getXMLNodeAsString(Node node) {
    return getXMLNodeAsString(node, "");
  }

  /**
   * Transforms an XML node (from a JAXP XML document) into a formatted string.
   * 
   * @param node
   *          The node to transform.
   * @return A string representation of the node.
   */
  public static String getXMLNodeAsString(Node node, String cdataElements) {
    StringWriter writer = new StringWriter();

    Transformer serializer;

    try {
      serializer = TransformerFactory.newInstance().newTransformer();
      if (cdataElements != "")
        serializer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS,
            cdataElements);
      serializer.setOutputProperty(OutputKeys.METHOD, "xml");
      serializer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
      serializer.setOutputProperty(OutputKeys.INDENT, "yes");
      serializer.setOutputProperty(OutputKeys.STANDALONE, "yes");
      serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      serializer.transform(new DOMSource(node), new StreamResult(writer));
    } catch (TransformerException e) {
      throw new GabotoRuntimeException(e);
    }
    return writer.toString();
  }

  /**
   * Creates a NamespaceContext object with the following list of namespaces:
   * 
   * <dl>
   * <dt>tei</dt>
   * <dd>http://www.tei-c.org/ns/1.0</dd>
   * </dl>
   * 
   * @return A NamespaceContext object
   */
  public static NamespaceContext getTEINamespaceContext() {
    NamespaceContext ctx = new NamespaceContext() {
      public String getNamespaceURI(String prefix) {
        String uri;
        if (prefix.equals("tei"))
          uri = "http://www.tei-c.org/ns/1.0";
        else
          uri = null;

        return uri;
      }

      // Dummy implementation - not used!
      public Iterator<String> getPrefixes(String val) {
        return null;
      }

      // Dummy implemenation - not used!
      public String getPrefix(String uri) {
        return null;
      }
    };

    return ctx;
  }
}
