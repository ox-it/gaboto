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
package org.oucs.gaboto.model;

import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.oucs.gaboto.entities.GabotoEntity;
import org.oucs.gaboto.entities.pool.GabotoEntityPool;
import org.oucs.gaboto.entities.pool.GabotoEntityPoolConfiguration;
import org.oucs.gaboto.exceptions.CorruptDataException;
import org.oucs.gaboto.exceptions.EntityDoesNotExistException;
import org.oucs.gaboto.exceptions.EntityPoolInvalidConfigurationException;
import org.oucs.gaboto.exceptions.GabotoRuntimeException;
import org.oucs.gaboto.exceptions.ResourceDoesNotExistException;
import org.oucs.gaboto.timedim.TimeSpan;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Snapshots represent a flat extract from the data in Gaboto.
 * 
 * <p>
 * Gaboto works with a data model built on RDF Named Graphs. However, working with named
 * graphs is rather difficult and tool support is limited. Furthermore, the performance of
 * queries against named graphs is usually much less than acceptable. 
 * </p>
 * 
 * <p>
 * {@link GabotoSnapshot}s provide one solution to this problem. They allow you to represent
 * the part of the data you are interested in in one flat RDF graph that you can then easily
 * query against. Furthermore, snapshots can be used to automatically create the java representation of
 * {@link GabotoEntity}s using a {@link GabotoEntityPool}.
 * </p>
 * 
 * @author Arno Mittelbach
 * @version 0.1
 */
public class GabotoSnapshot {

	private Model model;
	
	private Gaboto gaboto;

	/**
	 * Creates a new snapshot using the Jena Model and Gaboto system.
	 * 
	 * @param model The Jena Model.
	 * @param gaboto The Gaboto system.
	 */
	public GabotoSnapshot(Model model, Gaboto gaboto) {
		 this.model = model;
		 this.gaboto = gaboto;
	}
	
	/**
	 * Returns the Gaboto model this snapshot is built upon.
	 * 
	 * @return The Gaboto model this snapshot is built upon.
	 */
	public Gaboto getGaboto(){
		return gaboto;
	}
	
	/**
	 * Returns the Jena model this snapshot is built upon.
	 * 
	 * @return The Jena model this snapshot is built upon.
	 */
	public Model getModel(){
		return model;
	}
	
	/**
	 * Returns the number of triples in the underlying model.
	 * 
	 * @return The number of triples in the underlying model.
	 */
	public long size(){
		return model.size();
	}

	/**
	 * Extracts all resources of a given type from the snapshot.
	 * 
	 * @param type The ontology type.
	 * 
	 * @return A collection of resources of the same type.
	 */
	public Collection<Resource> getResourcesOfType(OntClass type) {
		Set<Resource> resources = new HashSet<Resource>();

		ResIterator it = model.listResourcesWithProperty(RDF.type, type);
		while(it.hasNext())
			resources.add(it.nextResource());
		
		return resources;
	}
	
	/**
	 * Extracts additional time information for an entity (represented by its RDF Resource).
	 * 
	 * @param res The Resource.
	 * 
	 * @return The time span or null.
	 */
	public TimeSpan getTimeSpanForEntity(Resource res){
		return getTimeSpanForEntity(res.getURI());
	}
	
	/**
	 * Extracts additional time information for an entity (represented by its URI).
	 * 
	 * @param uri The entity's URI.
	 * 
	 * @return The time span or null.
	 */
	public TimeSpan getTimeSpanForEntity(String uri){
		try {
			return gaboto.getEntitysLifetime(uri);
		} catch (EntityDoesNotExistException e) {
	    return null;
		}
	}

	/**
	 * Extracts a Property object for a given URI.
	 * 
	 * @param propertyURI The property's URI
	 * 
	 * @return The Property.
	 */
	public Property getProperty(String propertyURI) {
		return model.getProperty(propertyURI);
	}

	/**
	 * Returns an RDF Resource.
	 * 
	 * @param uri The resource's URI
	 * 
	 * @return The RDF Resource
	 * 
	 * @throws ResourceDoesNotExistException 
	 */
	public Resource getResource(String uri) throws ResourceDoesNotExistException {
		if(! containsResource(uri))
			throw new ResourceDoesNotExistException(uri);
		
		return model.getResource(uri);
	}

	/**
	 * Tests if a resource with the specified URI exists.
	 * 
	 * @param uri The URI identifying the resource.
	 * @return True, if resource exists.
	 */
	public boolean containsResource(String uri) {
		return model.getGraph().contains(Node.createURI(uri), Node.ANY, Node.ANY);
	}

	/**
	 * Tests if a resource exists.
	 * 
	 * @param res The resource.
	 * @return True, if resource exists.
	 */
	public boolean containsResource(Resource res) {
		return containsResource(res.getURI());
	}
	
	public GabotoEntityPool loadEntitiesWithProperty(String propURI){
		return loadEntitiesWithProperty(getProperty(propURI));
	}
	
	/**
	 * Loads entities that have a certain property.
	 * 
	 * @param prop The property
	 * @return An entity pool with all entities that have this property.
	 */
	public GabotoEntityPool loadEntitiesWithProperty(Property prop){
		Collection<Resource> resources = new HashSet<Resource>();
		ResIterator it = model.listResourcesWithProperty(prop);
		while(it.hasNext())
			resources.add(it.nextResource());

		return loadEntityPoolFromResources(resources);
	}
	
	
	
	public GabotoEntityPool loadEntitiesWithProperty(String propURI, boolean value){
		Property prop = getProperty(propURI);
		if(null == prop)
			return new GabotoEntityPool(this.gaboto, this);
		
		return loadEntitiesWithProperty(getProperty(propURI), value);
	}
	
	/**
	 * Loads entities that have a certain property.
	 * 
	 * @param prop The property
	 * @param value The property's value
	 * @return An entity pool with all entities that have this property.
	 */
	public GabotoEntityPool loadEntitiesWithProperty(Property prop, boolean value){
		Collection<Resource> resources = new HashSet<Resource>();
		ResIterator it = model.listResourcesWithProperty(prop, value);
		while(it.hasNext())
			resources.add(it.nextResource());

		return loadEntityPoolFromResources(resources);
	}
	
	
	public GabotoEntityPool loadEntitiesWithProperty(String propURI, char value){
		Property prop = getProperty(propURI);
		if(null == prop)
			return new GabotoEntityPool(this.gaboto, this);
		
		return loadEntitiesWithProperty(getProperty(propURI), value);
	}
	
	/**
	 * Loads entities that have a certain property.
	 * 
	 * @param prop The property
	 * @param value The property's value
	 * @return An entity pool with all entities that have this property.
	 */
	public GabotoEntityPool loadEntitiesWithProperty(Property prop, char value){
		Collection<Resource> resources = new HashSet<Resource>();
		ResIterator it = model.listResourcesWithProperty(prop, value);
		while(it.hasNext())
			resources.add(it.nextResource());

		return loadEntityPoolFromResources(resources);
	}
	
	public GabotoEntityPool loadEntitiesWithProperty(String propURI, double value){
		Property prop = getProperty(propURI);
		if(null == prop)
			return new GabotoEntityPool(this.gaboto, this);
		
		return loadEntitiesWithProperty(getProperty(propURI), value);
	}
	
	/**
	 * Loads entities that have a certain property.
	 * 
	 * @param prop The property
	 * @param value The property's value
	 * @return An entity pool with all entities that have this property.
	 */
	public GabotoEntityPool loadEntitiesWithProperty(Property prop, double value){
		Collection<Resource> resources = new HashSet<Resource>();
		ResIterator it = model.listResourcesWithProperty(prop, value);
		while(it.hasNext())
			resources.add(it.nextResource());

		return loadEntityPoolFromResources(resources);
	}	
	
	public GabotoEntityPool loadEntitiesWithProperty(String propURI, float value){
		Property prop = getProperty(propURI);
		if(null == prop)
			return new GabotoEntityPool(this.gaboto, this);
		
		return loadEntitiesWithProperty(getProperty(propURI), value);
	}
	
	/**
	 * Loads entities that have a certain property.
	 * 
	 * @param prop The property
	 * @param value The property's value
	 * @return An entity pool with all entities that have this property.
	 */
	public GabotoEntityPool loadEntitiesWithProperty(Property prop, float value){
		Collection<Resource> resources = new HashSet<Resource>();
		ResIterator it = model.listResourcesWithProperty(prop, value);
		while(it.hasNext())
			resources.add(it.nextResource());

		return loadEntityPoolFromResources(resources);
	}	
	
	public GabotoEntityPool loadEntitiesWithProperty(String propURI, long value){
		Property prop = getProperty(propURI);
		if(null == prop)
			return new GabotoEntityPool(this.gaboto, this);
		
		return loadEntitiesWithProperty(getProperty(propURI), value);
	}
	
	/**
	 * Loads entities that have a certain property.
	 * 
	 * @param prop The property
	 * @param value The property's value
	 * @return An entity pool with all entities that have this property.
	 */
	public GabotoEntityPool loadEntitiesWithProperty(Property prop, long value){
		Collection<Resource> resources = new HashSet<Resource>();
		ResIterator it = model.listResourcesWithProperty(prop, value);
		while(it.hasNext())
			resources.add(it.nextResource());

		return loadEntityPoolFromResources(resources);
	}	
	
	public GabotoEntityPool loadEntitiesWithProperty(String propURI, String value){
		Property prop = getProperty(propURI);
    if (prop == null)
      throw new GabotoRuntimeException("Property not found: " + propURI);
		//if(null == prop)
		//	return new GabotoEntityPool(this.gaboto, this);
		
		return loadEntitiesWithProperty(prop, value);
	}
	
	public GabotoEntityPool loadEntitiesWithProperty(String propURI, Object value){
		Property prop = getProperty(propURI);
    if (prop == null)
      throw new GabotoRuntimeException("Property not found: " + propURI);
		//if(null == prop)
		//	return new GabotoEntityPool(this.gaboto, this);
		
		return loadEntitiesWithProperty(prop, value);
	}
	
	
	public GabotoEntityPool loadEntitiesWithProperty(Property prop, String value){
		return loadEntitiesWithProperty(prop, (Object) value);
	}
	
	/**
	 * Loads entities that have a certain property.
	 * 
	 * @param prop The property
	 * @param value The property's value
	 * @return An entity pool with all entities that have this property.
	 */
	public GabotoEntityPool loadEntitiesWithProperty(Property prop, Object value){
		Collection<Resource> resources = new HashSet<Resource>();
		ResIterator it = model.listResourcesWithProperty(prop, value);
		while(it.hasNext())
			resources.add(it.nextResource());

		return loadEntityPoolFromResources(resources);
	}	
	
	/**
	 * Loads entities that have a certain property.
	 * 
	 * @param prop The property
	 * @param value The property's value
	 * @return An entity pool with all entities that have this property.
	 */
	public GabotoEntityPool loadEntitiesWithProperty(Property prop, RDFNode value){
		Collection<Resource> resources = new HashSet<Resource>();
		ResIterator it = model.listResourcesWithProperty(prop, value);
		while(it.hasNext())
			resources.add(it.nextResource());

		return loadEntityPoolFromResources(resources);
	}	
	
	
	/**
	 * Creates an entity pool from some resources out of this snapshot
	 * 
	 * @param resources The collection of resources
	 * @return The entity pool
	 */
	private GabotoEntityPool loadEntityPoolFromResources(Collection<Resource> resources) {
		GabotoEntityPoolConfiguration config = new GabotoEntityPoolConfiguration(this);
		config.setResources(resources);
		config.setAddReferencedEntitiesToPool(false);
		
		try {
			return GabotoEntityPool.createFrom(config);
		} catch (EntityPoolInvalidConfigurationException e) {
			throw new GabotoRuntimeException(e);
		}
	}
	
	/**
	 * Loads a specific entity.
	 * 
	 * @param uri The URI of the entity that is to be created.
	 * 
	 * @return The entity.
	 * 
	 * @throws ResourceDoesNotExistException Thrown if the snapshot does not contain any information about then entity.
	 * @throws CorruptDataException
	 */
	public GabotoEntity loadEntity(String uri) throws ResourceDoesNotExistException {
		Collection<Resource> resCol = new HashSet<Resource>();
		
		Resource res = this.getResource(uri);
		resCol.add(res);
		
		// create config
		GabotoEntityPoolConfiguration config = new GabotoEntityPoolConfiguration(this);
		config.setResources(resCol);

		//
		GabotoEntityPool pool = null;
		try {
			pool = GabotoEntityPool.createFrom(config);
		} catch (EntityPoolInvalidConfigurationException e) {}
		
		return pool.getEntity(uri);
	}

	/**
	 * Executes a Construct SPARQL Query.
	 * 
	 * @param query The query to execute.
	 * 
	 * @return The resulting snapshot.
	 */
	public GabotoSnapshot execSPARQLConstruct(String query){
		QueryExecution qexec = QueryExecutionFactory.create( query, getModel() );
		Model model = null;
		try{
			 model = qexec.execConstruct();
		} finally { qexec.close(); }
		
		return new GabotoSnapshot(model, gaboto);
	}
	
	/**
	 * Executes a Describe SPARQL Query.
	 * 
	 * @param query The query to execute.
	 * 
	 * @return The resulting snapshot.
	 */
	public GabotoSnapshot execSPARQLDescribe(String query){
		QueryExecution qexec = QueryExecutionFactory.create( query, getModel() );
		Model model = null;
		try{
			 model = qexec.execDescribe();
		} finally { qexec.close(); }
		
		return new GabotoSnapshot(model, gaboto);
	}
	
	/**
	 * Executes an Ask SPARQL Query.
	 * 
	 * @param query The query to execute.
	 * 
	 * @return The resulting snapshot.
	 */
	public boolean execSPARQLAsk(String query){
		QueryExecution qexec = QueryExecutionFactory.create( query, getModel() );
		boolean result = false;
		try{
			 result = qexec.execAsk();
		} finally { qexec.close(); }
		return result;
	}
	
	/**
	 * Executes a Select SPARQL Query.
	 * 
	 * @param query The query to execute.
	 * 
	 * @return The resulting snapshot.
	 */
	public void execSPARQLSelect(String query, QuerySolutionProcessor processor){
		QueryExecution qexec = QueryExecutionFactory.create( query, getModel() );
		
		try{
			// execute query
			ResultSet results = qexec.execSelect();
		
			// iterate over results
			while(results.hasNext()){
				QuerySolution soln = results.nextSolution();
				
				// process solution
				processor.processSolution(soln);
				
				// continue ?
				if(processor.stopProcessing())
					break;
			}
		} finally { qexec.close(); }
	}
	
	/**
	 * Creates an {@link GabotoEntityPool} with a standard configuration from this snapshot.
	 * 
	 * @return An GabotoEntityPool build from this snapshot.
	 * @throws CorruptDataException 
	 */
	public GabotoEntityPool buildEntityPool() throws CorruptDataException{
		try {
			 return GabotoEntityPool.createFrom(new GabotoEntityPoolConfiguration(this));
		} catch(EntityPoolInvalidConfigurationException e){}
		
		// we should never reach this point
		assert(false);
		
		return null;
	}
	
	/**
	 * Searialize the model
	 * 
	 * @param os
	 */
	public void write(OutputStream os){
		model.write(os);
	}
	
	/**
	 * Searialize the model
	 * 
	 * @param os
	 * @param format
	 */
	public void write(OutputStream os, String format){
		model.write(os, format);
	}
}
