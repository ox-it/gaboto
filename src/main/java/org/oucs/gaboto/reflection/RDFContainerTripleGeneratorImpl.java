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
package org.oucs.gaboto.reflection;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.oucs.gaboto.beans.GabotoBean;
import org.oucs.gaboto.entities.GabotoEntity;
import org.oucs.gaboto.entities.utils.BagComplexProperty;
import org.oucs.gaboto.entities.utils.BagLiteralProperty;
import org.oucs.gaboto.entities.utils.BagURIProperty;
import org.oucs.gaboto.entities.utils.ComplexProperty;
import org.oucs.gaboto.entities.utils.GabotoEntityUtils;
import org.oucs.gaboto.entities.utils.SimpleLiteralProperty;
import org.oucs.gaboto.entities.utils.SimpleURIProperty;
import org.oucs.gaboto.exceptions.CorruptDataException;
import org.oucs.gaboto.exceptions.GabotoRuntimeException;
import org.oucs.gaboto.exceptions.IllegalAnnotationException;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.vocabulary.RDF;

public class RDFContainerTripleGeneratorImpl implements RDFContainerTripleGenerator {

	private static Logger logger = Logger.getLogger(RDFContainerTripleGeneratorImpl.class.getName());
	
	private static RDFContainerTripleGenerator instance;
	
	protected RDFContainerTripleGeneratorImpl(){}
	
	public static RDFContainerTripleGenerator getInstance(){
		if(instance == null)
			instance = new RDFContainerTripleGeneratorImpl();
		
		return instance;
	}
	
	/**
	 * Creates a list of RDF triples that represent this {@link GabotoEntity}.
	 * 
	 * <p>
	 * Same as: entity.getTriplesFor(true);
	 * </p>
	 * 
	 * @see #getTriplesFor(boolean)
	 * @return a list of triples that represent this entity.
	 * @throws IllegalAnnotationException 
	 */
	public List<Triple> getTriplesFor(RDFContainer rdfContainerObject, Node subjectNode) 
	    throws IllegalAnnotationException{
		return getTriplesFor(rdfContainerObject, subjectNode, true);
	}
	
	
	/**
	 * Creates a list of RDF triples that represent this {@link GabotoEntity}.
	 * 
	 * @param includeType Whether or not a triple denoting the entities type should be added to the list of triples.
	 * 
	 * @return a list of triples that represent this entity.
	 * 
	 * @throws IllegalAnnotationException 
	 */
	public List<Triple> getTriplesFor(RDFContainer rdfContainerObject, Node subjectNode, boolean includeType) 
	    throws IllegalAnnotationException{
		List<Triple> triples = new ArrayList<Triple>();
		
		// add the entities type
		if(includeType){
			triples.add(new Triple(
					subjectNode,
					Node.createURI(RDF.type.getURI()),
					Node.createURI(rdfContainerObject.getType())
			));
		}
		
		// loop over methods to identify simple and complex properties
		for(Method method : rdfContainerObject.getClass().getMethods()){
			// is it annotated as a simple property
			if(method.isAnnotationPresent(SimpleURIProperty.class)){
				SimpleURIProperty anno = method.getAnnotation(SimpleURIProperty.class);
				
				// is it a get
				if(! GabotoEntityUtils.IsGETMethod(method))
					continue;
				
				// in the case of a simple property we can construct the triple directly
				String propertyURI = anno.value();
				
				//process
				getTriplesFor_SimpleURIProperty(rdfContainerObject, subjectNode, triples, propertyURI, method);
				
			} else if(method.isAnnotationPresent(SimpleLiteralProperty.class)){ // complex property?
				SimpleLiteralProperty anno = method.getAnnotation(SimpleLiteralProperty.class);
				
				// is it a get
				if(! GabotoEntityUtils.IsGETMethod(method))
					continue;
				
				// in the case of a simple property we can construct the triple directly
				String propertyURI = anno.value();
				
				//process
				getTriplesFor_SimpleLiteralProperty(rdfContainerObject, subjectNode, triples, propertyURI, method);
			} else if(method.isAnnotationPresent(ComplexProperty.class)){ // complex property?
				ComplexProperty anno = method.getAnnotation(ComplexProperty.class);
				
				// is it a get
				if(! GabotoEntityUtils.IsGETMethod(method))
					continue;
				
				String propertyURI = anno.value();
				
				// in the case of a simple property we can construct the triple directly
				getTriplesFor_ComplexProperty(rdfContainerObject, subjectNode, triples, propertyURI, method);
			} else if(method.isAnnotationPresent(BagURIProperty.class)){
				BagURIProperty anno = method.getAnnotation(BagURIProperty.class);
				
				// is it a set method
				if(! GabotoEntityUtils.IsGETMethod(method))
					continue;
				
				// get propertyURI from annotation
				String propertyURI = anno.value();
				
				// process
				getTriplesFor_BagURIProperty(rdfContainerObject, subjectNode, triples, propertyURI, method);
			} else if(method.isAnnotationPresent(BagLiteralProperty.class)){
				BagLiteralProperty anno = method.getAnnotation(BagLiteralProperty.class);
				
				// is it a set method
				if(! GabotoEntityUtils.IsGETMethod(method))
					continue;
				
				// get propertyURI from annotation
				String propertyURI = anno.value();
				
				// process
				getTriplesFor_BagLiteralProperty(rdfContainerObject, subjectNode, triples, propertyURI, method);
			} else if(method.isAnnotationPresent(BagComplexProperty.class)){
				BagComplexProperty anno = method.getAnnotation(BagComplexProperty.class);
				
				// is it a set method
				if(! GabotoEntityUtils.IsGETMethod(method))
					continue;
				
				// get propertyURI from annotation
				String propertyURI = anno.value();
				
				// process
				getTriplesFor_BagComplexProperty(rdfContainerObject, subjectNode, triples, propertyURI, method);
			}
		}
		
		return triples;
	}
	


	/**
	 * Helper method to create the triples for a get method tagged with the {@link SimpleURIProperty} annotation.
	 * 
	 * @see #getTriplesFor(GabotoEntity, boolean)
	 * 
	 * @param triples
	 * @param entity
	 * @param propertyURI
	 * @param method
	 */
	private void getTriplesFor_SimpleURIProperty(
	    Object rdfContainerObject, Node subjectNode, List<Triple> triples, String propertyURI, Method method) {
		try {
			Object object = method.invoke(rdfContainerObject, (Object[])null);
			if(object == null )
				return;
			
			String objectURI = "";
			if(object instanceof String)
				objectURI = (String) object;
			else if(object instanceof GabotoEntity)
				objectURI = ((GabotoEntity)object).getUri();
			else
				throw new Exception();
			
			triples.add(new Triple(
				subjectNode,
				Node.createURI(propertyURI),
				Node.createURI(objectURI)
			));
			
    } catch (Exception e) {
      throw new GabotoRuntimeException(e);
    } 
	}
	
	/**
	 * Helper method to create the triples for a get method tagged with the {@link SimpleLiteralProperty} annotation.
	 * 
	 * @see #getTriplesFor(GabotoEntity, boolean)
	 * 
	 * @param triples
	 * @param entity
	 * @param propertyURI
	 * @param method
	 */
	private void getTriplesFor_SimpleLiteralProperty(Object rdfContainerObject, Node subjectNode, List<Triple> triples, String propertyURI, Method method) {
		try {
			Object object = method.invoke(rdfContainerObject, (Object[])null);
			if(object == null)
				return;
			
			// find datatype
			RDFDatatype datatype = null;
			SimpleLiteralProperty annotation = method.getAnnotation(SimpleLiteralProperty.class);
			
			if(annotation.javaType().toLowerCase().equals("string"))
				datatype = XSDDatatype.XSDstring;
			else if(annotation.javaType().toLowerCase().equals("integer") || annotation.javaType().toLowerCase().equals("int") )
				datatype = XSDDatatype.XSDint;
			else if(annotation.javaType().toLowerCase().equals("float"))
				datatype = XSDDatatype.XSDfloat;
			else if(annotation.javaType().toLowerCase().equals("double"))
				datatype = XSDDatatype.XSDdouble;
			else if(annotation.javaType().toLowerCase().equals("boolean"))
				datatype = XSDDatatype.XSDboolean;
			else {
				throw new IllegalArgumentException("Unrecognized literal type: " + annotation.javaType());
			}
			
			triples.add(new Triple(
				subjectNode,
				Node.createURI(propertyURI),
				Node.createLiteral(String.valueOf(object), null, datatype)
			));
    } catch (Exception e) {
      throw new GabotoRuntimeException(e);
    } 
	
	}
	
	/**
	 * Helper method to create the triples for a get method tagged with the {@link ComplexProperty} annotation.
	 * 
	 * @see #getTriplesFor(GabotoEntity, boolean)
	 * 
	 * @param triples
	 * @param propertyURI 
	 * @param entity
	 * @param propertyURI
	 * @param method
	 */
	private void getTriplesFor_ComplexProperty(Object rdfContainerObject, Node subjectNode, List<Triple> triples, String propertyURI, Method method) {
		try {
			Object object = method.invoke(rdfContainerObject, (Object[])null);
			
			if( object == null)
				return;
			
			if(! (object instanceof GabotoBean))
				throw new Exception();
			
			GabotoBean bean = (GabotoBean) object;
			
			Node blankBeanNode = GabotoEntityUtils.createAnonForBean(subjectNode.getURI(), propertyURI);
			triples.add(new Triple(
					subjectNode,
					Node.createURI(propertyURI),
					blankBeanNode
				));
			
			triples.addAll(bean.getCorrespondingRDFTriples(blankBeanNode));
    } catch (Exception e) {
      throw new GabotoRuntimeException(e);
    } 
	}

	/**
	 * Helper method to create the triples for a get method tagged with the {@link BagComplexProperty} annotation.
	 * 
	 * @see #getTriplesFor(GabotoEntity, boolean)
	 * 
	 * @param triples
	 * @param entity
	 * @param propertyURI
	 * @param method
	 * @throws IllegalAnnotationException 
	 */
	@SuppressWarnings("unchecked")
	private void getTriplesFor_BagComplexProperty(Object rdfContainerObject, Node subjectNode, List<Triple> triples, String propertyURI, Method method) throws IllegalAnnotationException {
		// create bag
		Node bag = GabotoEntityUtils.createAnonForBag(subjectNode.getURI(), propertyURI);
		triples.add(new Triple(
			bag,
			Node.createURI(RDF.type.getURI()),
			Node.createURI(RDF.Bag.getURI())
		));
		
		// add bag to entity
		triples.add(new Triple(
			subjectNode,
			Node.createURI(propertyURI),
			bag
		));
		
		// fill bag
		try {
			Object object = method.invoke(rdfContainerObject, (Object[])null);
			
      if(object == null)
				return;
			
			if(! (object instanceof Collection) )
				throw new IllegalAnnotationException(rdfContainerObject.getClass());
			
			// loop over collection
			int count = 1;
			for(Object o : (Collection) object){
				GabotoBean bean = (GabotoBean) o;
				
				// create blank node
				Node blankBeanNode = GabotoEntityUtils.createAnonForBean(subjectNode.getURI(), propertyURI);
				triples.addAll(bean.getCorrespondingRDFTriples(blankBeanNode));
				
				// add blank node to bag
				triples.add(new Triple(
					bag,
					Node.createURI(RDF.li(count).getURI()),
					blankBeanNode
				));
				
				count++;
			}
			
    } catch (Exception e) {
      throw new GabotoRuntimeException(e);
    } 
	}

	/**
	 * Helper method to create the triples for a get method tagged with the {@link BagLiteralProperty} annotation.
	 * 
	 * @see #getTriplesFor(GabotoEntity, boolean)
	 * 
	 * @param triples
	 * @param entity
	 * @param propertyURI
	 * @param method
	 * @throws IllegalAnnotationException 
	 */
	@SuppressWarnings("unchecked")
	private void getTriplesFor_BagLiteralProperty(Object rdfContainerObject, Node subjectNode, List<Triple> triples, String propertyURI, Method method) throws IllegalAnnotationException {
		// create bag
		Node bag = GabotoEntityUtils.createAnonForBag(subjectNode.getURI(), propertyURI);
		triples.add(new Triple(
			bag,
			Node.createURI(RDF.type.getURI()),
			Node.createURI(RDF.Bag.getURI())
		));
		
		// add bag to entity
		triples.add(new Triple(
			subjectNode,
			Node.createURI(propertyURI),
			bag
		));
		
		// fill bag
			Object object;
      try {
        object = method.invoke(rdfContainerObject, (Object[])null);
      } catch (Exception e) {
        throw new GabotoRuntimeException(e);
      }
			
      if(object == null)
				return;
			
			if(! (object instanceof Collection) )
				throw new IllegalAnnotationException(rdfContainerObject.getClass());
			
			// find datatype
			RDFDatatype datatype = null;
			BagLiteralProperty annotation = method.getAnnotation(BagLiteralProperty.class);
			
			if(annotation.javaType().toLowerCase().equals("string"))
				datatype = XSDDatatype.XSDstring;
			else if(annotation.javaType().toLowerCase().equals("integer") || annotation.javaType().toLowerCase().equals("int") )
				datatype = XSDDatatype.XSDint;
			else if(annotation.javaType().toLowerCase().equals("float"))
				datatype = XSDDatatype.XSDfloat;
			else if(annotation.javaType().toLowerCase().equals("double"))
				datatype = XSDDatatype.XSDdouble;
			else if(annotation.javaType().toLowerCase().equals("boolean"))
				datatype = XSDDatatype.XSDboolean;
			else {
				throw new IllegalArgumentException("Unrecognized literal type: " + annotation.javaType());
			}
			
			// loop over collection
			int count = 1;
			for(Object o : (Collection) object){
				triples.add(new Triple(
					bag,
					Node.createURI(RDF.li(count).getURI()),
					Node.createLiteral((String)o)
				));
				count++;
			}
			
	}

	/**
	 * Helper method to create the triples for a get method tagged with the {@link BagURIProperty} annotation.
	 * 
	 * @see #getTriplesFor(GabotoEntity, boolean)
	 * 
	 * @param triples
	 * @param entity
	 * @param propertyURI
	 * @param method
	 */
	@SuppressWarnings("unchecked")
	private void getTriplesFor_BagURIProperty(Object rdfContainerObject, Node subjectNode, List<Triple> triples, String propertyURI, Method method) throws IllegalAnnotationException {
		// create bag
		Node bag = GabotoEntityUtils.createAnonForBag(subjectNode.getURI(), propertyURI);
		triples.add(new Triple(
			bag,
			Node.createURI(RDF.type.getURI()),
			Node.createURI(RDF.Bag.getURI())
		));
		
		// add bag to entity
		triples.add(new Triple(
			subjectNode,
			Node.createURI(propertyURI),
			bag
		));
		
		
		// fill bag
		try {
			Object object = method.invoke(rdfContainerObject, (Object[])null);
			
      if(object == null)
				return;
			
			if(! (object instanceof Collection) )
				throw new IllegalAnnotationException(rdfContainerObject.getClass());
			
			// loop over collection
			int count = 1;
			for(Object o : (Collection) object){
				if(o == null)
					throw new CorruptDataException("Bag properties may not contain null values.");
				triples.add(new Triple(
					bag,
					Node.createURI(RDF.li(count).getURI()),
					Node.createURI(((GabotoEntity)o).getUri())
				));
				count++;
			}
			
    } catch (Exception e) {
      throw new GabotoRuntimeException(e);
    } 
	}
	
}
