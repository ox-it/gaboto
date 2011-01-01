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
package net.sf.gaboto.node;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.ac.ox.oucs.oxpoints.gaboto.beans.OnlineAccount;

import net.sf.gaboto.GabotoRuntimeException;
import net.sf.gaboto.IncoherenceException;
import net.sf.gaboto.node.annotation.BagComplexProperty;
import net.sf.gaboto.node.annotation.BagLiteralProperty;
import net.sf.gaboto.node.annotation.BagResourceProperty;
import net.sf.gaboto.node.annotation.BagURIProperty;
import net.sf.gaboto.node.annotation.ComplexProperty;
import net.sf.gaboto.node.annotation.ResourceProperty;
import net.sf.gaboto.node.annotation.SimpleLiteralProperty;
import net.sf.gaboto.node.annotation.SimpleURIProperty;


import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.vocabulary.RDF;

public class RDFTypedTriplesListFactoryImpl implements RDFTypedTriplesListFactory {

  private static RDFTypedTriplesListFactory instance;

  protected RDFTypedTriplesListFactoryImpl() {
  }

  public static RDFTypedTriplesListFactory getInstance() {
    if (instance == null)
      instance = new RDFTypedTriplesListFactoryImpl();

    return instance;
  }

  public List<Triple> getTriplesFor(RDFTyped rdfContainerObject, Node subjectNode) {
    return getTriplesFor(rdfContainerObject, subjectNode, true);
  }

  public List<Triple> getTriplesFor(RDFTyped rdfContainerObject, Node subjectNode, boolean includeType) {
	List<Triple> triples = new ArrayList<Triple>();

    // add the entity's type
    if (includeType) {
      triples.add(new Triple(subjectNode, Node.createURI(RDF.type.getURI()), Node.createURI(rdfContainerObject
              .getType())));
    }

    // loop over methods to identify simple and complex properties
    for (Method method : rdfContainerObject.getClass().getMethods()) {
      // is it annotated as a simple property
      if (method.isAnnotationPresent(SimpleURIProperty.class)) {
        SimpleURIProperty anno = method.getAnnotation(SimpleURIProperty.class);

        // is it a get
        if (!GabotoEntityUtils.IsGETMethod(method))
          continue;

        // in the case of a simple property we can construct the triple directly
        String propertyURI = anno.value();

        // process
        getTriplesFor_SimpleURIProperty(rdfContainerObject, subjectNode, triples, propertyURI, method);

      } else if (method.isAnnotationPresent(SimpleLiteralProperty.class)) { // complex
                                                                            // property?
        SimpleLiteralProperty anno = method.getAnnotation(SimpleLiteralProperty.class);

        // is it a get
        if (!GabotoEntityUtils.IsGETMethod(method))
          continue;

        // in the case of a simple property we can construct the triple directly
        String propertyURI = anno.value();

        // process
        getTriplesFor_SimpleLiteralProperty(rdfContainerObject, subjectNode, triples, propertyURI, method);
      
      } else if (method.isAnnotationPresent(ResourceProperty.class)) {
    	  ResourceProperty anno = method.getAnnotation(ResourceProperty.class);
    	  if (!GabotoEntityUtils.IsGETMethod(method))
    			  continue;
    	  Node propertyURI = Node.createURI(anno.value());
    	  String resource = (String) invokeMethod(rdfContainerObject,method);
    	  if (resource != null)
    		  triples.add(new Triple(subjectNode, propertyURI, Node.createURI(resource)));
      } else if (method.isAnnotationPresent(ComplexProperty.class)) { // complex
                                                                      // property?
        ComplexProperty anno = method.getAnnotation(ComplexProperty.class);

        // is it a get
        if (!GabotoEntityUtils.IsGETMethod(method))
          continue;

        String propertyURI = anno.value();

        // in the case of a simple property we can construct the triple directly
        getTriplesFor_ComplexProperty(rdfContainerObject, subjectNode, triples, propertyURI, method);
      } else if (method.isAnnotationPresent(BagURIProperty.class)) {
        BagURIProperty anno = method.getAnnotation(BagURIProperty.class);

        // is it a set method
        if (!GabotoEntityUtils.IsGETMethod(method))
          continue;

        // get propertyURI from annotation
        String propertyURI = anno.value();

        // process
        getTriplesFor_BagURIProperty(rdfContainerObject, subjectNode, triples, propertyURI, method);
      } else if (method.isAnnotationPresent(BagLiteralProperty.class)) {
        BagLiteralProperty anno = method.getAnnotation(BagLiteralProperty.class);

        // is it a set method
        if (!GabotoEntityUtils.IsGETMethod(method))
          continue;

        // get propertyURI from annotation
        String propertyURI = anno.value();

        // process
        getTriplesFor_BagLiteralProperty(rdfContainerObject, subjectNode, triples, propertyURI, method);
      } else if (method.isAnnotationPresent(BagComplexProperty.class)) {
        BagComplexProperty anno = method.getAnnotation(BagComplexProperty.class);

        // is it a set method
        if (!GabotoEntityUtils.IsGETMethod(method))
          continue;

        // get propertyURI from annotation
        String propertyURI = anno.value();

        // process
        getTriplesFor_BagComplexProperty(rdfContainerObject, subjectNode, triples, propertyURI, method);
      } else if (method.isAnnotationPresent(BagResourceProperty.class)) {
    	  BagResourceProperty anno = method.getAnnotation(BagResourceProperty.class);
    	  if (!GabotoEntityUtils.IsGETMethod(method))
    			  continue;
    	  Node propertyURI = Node.createURI(anno.value());
    	  Collection<?> resources = (Collection<?>) invokeMethod(rdfContainerObject,method);
    	  if (resources != null)
    		  for (Object resource : resources) {
    			  if (resource instanceof String)
    				  triples.add(new Triple(subjectNode, propertyURI, Node.createURI((String) resource)));
    		  }
      }
    }

    return triples;
  }

  /**
   * Helper method to create the triples for a get method tagged with the
   * {@link SimpleURIProperty} annotation.
   * 
   * @see #getTriplesFor(GabotoEntity, boolean)
   * 
   * @param triples
   * @param entity
   * @param propertyURI
   * @param method
   */
  private void getTriplesFor_SimpleURIProperty(Object rdfContainerObject, Node subjectNode, List<Triple> triples,
          String propertyURI, Method method) {
      Object object = invokeMethod(rdfContainerObject,method);
      if (object == null)
        return;

      String objectURI = "";
      if (object instanceof String)
        objectURI = (String) object;
      else if (object instanceof GabotoEntity)
        objectURI = ((GabotoEntity) object).getUri();
      else
        throw new GabotoRuntimeException("Found object of class " + object.getClass() + 
                " (" + object + ") when expecting a String or GabotoEntity");

      triples.add(new Triple(subjectNode, Node.createURI(propertyURI), Node.createURI(objectURI)));

  }

  /**
   * Helper method to create the triples for a get method tagged with the
   * {@link SimpleLiteralProperty} annotation.
   * 
   * @see #getTriplesFor(GabotoEntity, boolean)
   * 
   * @param triples
   * @param entity
   * @param propertyURI
   * @param method
   */
  private void getTriplesFor_SimpleLiteralProperty(Object rdfContainerObject, Node subjectNode, List<Triple> triples,
          String propertyURI, Method method) {
    Object object = invokeMethod(rdfContainerObject,method);
    if (object == null)
      return;

    SimpleLiteralProperty annotation = method.getAnnotation(SimpleLiteralProperty.class);
    RDFDatatype datatype = getDatatypeForAnnotation(annotation);


    triples.add(new Triple(subjectNode, Node.createURI(propertyURI), Node.createLiteral(String.valueOf(object), null,
            datatype)));
  }

  /**
   * Helper method to create the triples for a get method tagged with the
   * {@link ComplexProperty} annotation.
   * 
   * @see #getTriplesFor(GabotoEntity, boolean)
   * 
   * @param triples
   * @param propertyURI
   * @param entity
   * @param propertyURI
   * @param method
   */
  private void getTriplesFor_ComplexProperty(Object rdfContainerObject, Node subjectNode, List<Triple> triples,
          String propertyURI, Method method) {
    Object object = invokeMethod(rdfContainerObject,method);

    if (object == null)
      return;

    if (!(object instanceof GabotoBean))
      throw new GabotoRuntimeException("Found object of class " + object.getClass() + 
              " (" + object + ") when expecting a GabotoBean");

    GabotoBean bean = (GabotoBean) object;

    Node blankBeanNode = GabotoEntityUtils.createAnonForBean(subjectNode.getURI(), propertyURI, bean);
    triples.add(new Triple(subjectNode, Node.createURI(propertyURI), blankBeanNode));

    triples.addAll(bean.getCorrespondingRDFTriples(blankBeanNode));
  }

  /**
   * Helper method to create the triples for a get method tagged with the
   * {@link BagComplexProperty} annotation.
   * 
   * @see #getTriplesFor(GabotoEntity, boolean)
   * 
   * @param triples
   * @param entity
   * @param propertyURI
   * @param method
   */
  private void getTriplesFor_BagComplexProperty(Object rdfContainerObject, Node subjectNode, List<Triple> triples,
          String propertyURI, Method method) {
	  
    Object object = invokeMethod(rdfContainerObject,method);

    if (object == null)
      return;

    if (!(object instanceof Collection<?>))
      throw new IllegalAnnotationException(rdfContainerObject.getClass());

    // loop over collection
    int count = 1;
    for (Object o : (Collection<?>) object) {
      GabotoBean bean = (GabotoBean) o;

      // create blank node
      Node blankBeanNode = GabotoEntityUtils.createAnonForBean(subjectNode.getURI(), propertyURI, bean);
      triples.addAll(bean.getCorrespondingRDFTriples(blankBeanNode));

      // add blank node to bag
      triples.add(new Triple(subjectNode, Node.createURI(propertyURI), blankBeanNode));

      count++;
    }

  }

  /**
   * Helper method to create the triples for a get method tagged with the
   * {@link BagLiteralProperty} annotation.
   * 
   * @see #getTriplesFor(GabotoEntity, boolean)
   * 
   * @param triples
   * @param entity
   * @param propertyURI
   * @param method
   */
  private void getTriplesFor_BagLiteralProperty(Object rdfContainerObject, Node subjectNode, List<Triple> triples,
		  String propertyURI, Method method) {


	  Object object = invokeMethod(rdfContainerObject,method);
	  if (object == null)
		  return;

	  if (!(object instanceof Collection<?>))
		  throw new IllegalAnnotationException(rdfContainerObject.getClass());

	  BagLiteralProperty annotation = method.getAnnotation(BagLiteralProperty.class);
	  RDFDatatype datatype = getDatatypeForAnnotation(annotation);
	  
	  int count = 1;
	  for (Object o : (Collection<?>) object) {
		  triples.add(new Triple(subjectNode, Node.createURI(propertyURI), Node.createLiteral((String) o, null, datatype)));
		  count++;
	  }

  }

  /**
   * Helper method to create the triples for a get method tagged with the
   * {@link BagURIProperty} annotation.
   * 
   * @see #getTriplesFor(GabotoEntity, boolean)
   * 
   * @param triples
   * @param entity
   * @param propertyURI
   * @param method
   */
  @SuppressWarnings("unchecked")
  private void getTriplesFor_BagURIProperty(Object rdfContainerObject, Node subjectNode, List<Triple> triples,
          String propertyURI, Method method) {
    // fill bag
    Object object = invokeMethod(rdfContainerObject,method);

    if (object == null)
      return;

    if (!(object instanceof Collection))
      throw new IllegalAnnotationException(rdfContainerObject.getClass());

    // loop over collection
    int count = 1;
    for (Object o : (Collection<Object>) object) {
      if (o == null)
        throw new IncoherenceException("Bag properties may not contain null values.");
      triples
              .add(new Triple(subjectNode, Node.createURI(propertyURI), Node
                      .createURI(((GabotoEntity) o).getUri())));
      count++;
    }

  }
  
  private RDFDatatype getDatatypeForAnnotation(Object annotation) {
	  String type;
	  
	  if (annotation instanceof SimpleLiteralProperty)
		  type = ((SimpleLiteralProperty) annotation).javaType().toLowerCase();
	  else if (annotation instanceof BagLiteralProperty)
		  type = ((BagLiteralProperty) annotation).javaType().toLowerCase();
	  else
		  throw new IllegalArgumentException("Argument must be either BagLiteralProperty or SimpleLiteralProperty.");
	  
	  if (type.equals("string"))
	  	  return XSDDatatype.XSDstring;
	  else if (type.equals("integer") || type.equals("int"))
		  return XSDDatatype.XSDint;
	  else if (type.equals("float"))
		  return XSDDatatype.XSDfloat;
	  else if (type.equals("double"))
		  return XSDDatatype.XSDdouble;
	  else if (type.equals("boolean"))
		  return XSDDatatype.XSDboolean;
	  else if (type.equals("datetime"))
		  return XSDDatatype.XSDdateTime;
	  else if (type.equals("date"))
		  return XSDDatatype.XSDdate;
	  else {
		  throw new IllegalArgumentException("Unrecognized literal type: " + type);
	  }
  }

  
  private Object invokeMethod(Object o, Method m) { 
    try {
      return m.invoke(o, (Object[]) null);
    } catch (Exception e) {
      throw new GabotoRuntimeException(e);
    }
  }
}
