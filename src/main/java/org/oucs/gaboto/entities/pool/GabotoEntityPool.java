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
package org.oucs.gaboto.entities.pool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.oucs.gaboto.entities.GabotoEntity;
import org.oucs.gaboto.entities.pool.filters.EntityFilter;
import org.oucs.gaboto.entities.pool.filters.ResourceFilter;
import org.oucs.gaboto.exceptions.CorruptDataException;
import org.oucs.gaboto.exceptions.EntityClassNotFoundException;
import org.oucs.gaboto.exceptions.EntityDoesNotExistException;
import org.oucs.gaboto.exceptions.EntityPoolInvalidConfigurationException;
import org.oucs.gaboto.exceptions.GabotoRuntimeException;
import org.oucs.gaboto.exceptions.ResourceDoesNotExistException;
import org.oucs.gaboto.model.Gaboto;
import org.oucs.gaboto.model.GabotoSnapshot;
import org.oucs.gaboto.model.QuerySolutionProcessor;
import org.oucs.gaboto.util.GabotoOntologyLookup;
import org.oucs.gaboto.util.GabotoPredefinedQueries;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * A collection of {@link GabotoEntity}s that for some reason belong together.
 * 
 * <p>
 * An {@link GabotoEntityPool} can be seen as a collection of {@link GabotoEntity}s that 
 * have something in common. {@link GabotoEntityPool}s can be created by simply adding entities
 * "by hand" or they can be automatically created from an {@link GabotoSnapshot} or a Jena {@link Model}.
 * </p>
 * 
 * <p>
 * The automatic creation is configured using a {@link GabotoEntityPoolConfiguration} object.
 * </p>
 * 
 * @author Arno Mittelbach
 * @version 0.1
 * 
 * @see GabotoEntityPoolConfiguration
 */
public class GabotoEntityPool {

	private static Logger logger = Logger.getLogger(GabotoEntityPool.class.getName());
	
	public static final int PASSIVE_PROPERTY_COLLECTION_TYPE_NONE = 1;
	public static final int PASSIVE_PROPERTY_COLLECTION_TYPE_BAG = 2;
	
	private Map<String, GabotoEntity> entities = new HashMap<String, GabotoEntity>();
	private Map<String, GabotoEntity> referencedEntities = new HashMap<String, GabotoEntity>();
	
	private HashSet<String> directEntities = new HashSet<String>();	
	
	private GabotoEntityPoolConfiguration config;
	
	private Gaboto gaboto;
	
	private GabotoSnapshot snapshot;
	
	/**
	 * Creates a new, empty entity pool.
	 * 
	 * <p>
	 * If you want to be able to load passive properties, supply the snapshot
	 * you are working with.
	 * </p>
	 * @see #GabotoEntityPool(Gaboto, GabotoSnapshot)
	 */
	public GabotoEntityPool(Gaboto gaboto){
		this.gaboto = gaboto;
	}
	
	
	public GabotoEntityPool(Gaboto gaboto, GabotoSnapshot snapshot){
		this.gaboto = gaboto;
		this.snapshot = snapshot;
	}
	
	
	/**
	 * Creates a new, empty entity pool with a given configuration.
	 * 
	 * <p>
	 * The configuration can be used to define {@link EntityFilter}s.
	 * </p>
	 * 
	 * @param config The configuration.
	 * @throws EntityPoolInvalidConfigurationException 
	 */
	public GabotoEntityPool(GabotoEntityPoolConfiguration config) throws EntityPoolInvalidConfigurationException{
		this.gaboto = config.getGaboto();
		this.config = config;
	}
	
	/**
   * Creates a new entity pool from a given configuration.
	 * 
	 * @param config The configuration
	 * @return a new entity pool created from a given configuration. 
	 * 
	 * @throws EntityPoolInvalidConfigurationException
	 */
	public static GabotoEntityPool createFrom(GabotoEntityPoolConfiguration config) 
	    throws EntityPoolInvalidConfigurationException{
		config.testConfiguration();
		
		// get snapshot
		GabotoSnapshot snapshot = config.getSnapshot();
		if(snapshot == null)
			snapshot = new GabotoSnapshot(config.getModel(), config.getGaboto());
		
		if(config.isUseResourceCollection())
			return createFrom(config, snapshot, config.getResources());
		
		return createFrom(config, snapshot);
	}

	/**
   * Creates a new entity pool from a given snapshot.
   * 
	 * @param snapshot snapshot to use
	 * @return a GabotoEntityPool
	 */
	public static GabotoEntityPool createFrom(GabotoSnapshot snapshot) { 
	  return createFrom(new GabotoEntityPoolConfiguration(snapshot), snapshot);
	}
	/**
	 * Creates an entity pool from a snapshot and a given configuration.
	 * 
	 * @param config
	 * @param snapshot 
	 * @return The created entity pool.
	 */
	private static GabotoEntityPool createFrom(GabotoEntityPoolConfiguration config, GabotoSnapshot snapshot) {
		GabotoEntityPool pool = new GabotoEntityPool(snapshot.getGaboto());
		pool.config = config;
		pool.snapshot = snapshot;
		
		Model model = snapshot.getModel();
		
		logger.debug("Attempting to create entity pool from model. This involves a lot of inflection and is error prone.");
		
		// get all types 
		Collection<String> registeredTypes = GabotoOntologyLookup.getRegisteredClassesAsURIs();
		for(String type : registeredTypes){
			if(! config.getAcceptedTypes().isEmpty() && ! config.getAcceptedTypes().contains(type))
				continue;
			
			if(! config.getUnacceptedTypes().isEmpty() && config.getUnacceptedTypes().contains(type))
				continue;
			
			try {
			  logger.debug("Loading type " + type);
				Class<?> entityClass = GabotoOntologyLookup.getEntityClassFor(type);
				
				// get everything in the model of that type
				// @todo (should be careful here if we have an inferencing model)
				
				ResIterator it = model.listSubjectsWithProperty(RDF.type, snapshot.getProperty(type));
				while(it.hasNext()){
					Resource res = it.nextResource();

					// instantiate
					GabotoEntity entity = (GabotoEntity) entityClass.newInstance();
					
					// resource filters
					boolean passedFilter = true;
					for(ResourceFilter filter : config.getResourceFilters()){
						try{
							filter.appliesTo().cast(entity);
							if( ! filter.filterResource(res)){
								passedFilter = false;
								break;
							}
						} catch(ClassCastException e){}
					}
					if(!passedFilter)
						continue;
					
					entity.loadFromSnapshot(res, snapshot, pool);
					
					// add entity
					pool.addEntity(entity);
					logger.debug("  Added " + entity);
					
				}
			} catch (InstantiationException e) {
        throw new GabotoRuntimeException(e);
			} catch (IllegalAccessException e) {
        throw new GabotoRuntimeException(e);
			}
		}
		
		if(config.isCreatePassiveEntities())
			pool.addPassiveEntities();
		
		// filter entities
		pool.filterEntities(config.getEntityFilters());

		
		return pool;
	}

	/**
	 * Creates an entity pool from a snapshot with all the resources that are specified in the supplied collection.
	 * 
	 * @param config
	 * @param snapshot
	 * @param resources
	 * @return The created entity pool.
	 */
	private static GabotoEntityPool createFrom(GabotoEntityPoolConfiguration config, GabotoSnapshot snapshot, Collection<Resource> resources) {
		GabotoEntityPool pool = new GabotoEntityPool(snapshot.getGaboto());
		pool.config = config;
		pool.snapshot = snapshot;
		
		logger.debug("Attempting to create pool from model and a set of resources.");
		
		// loop over resources
		for(Resource res : resources){
			try{
				pool.addEntity(res, snapshot);
			} catch(EntityClassNotFoundException e){
				logger.info(e.getMessage());
			} catch (ResourceDoesNotExistException e) {
				e.printStackTrace();
			} catch (EntityDoesNotExistException e) {
				e.printStackTrace();
			}
		}

		//
		if(config.isCreatePassiveEntities())
			pool.addPassiveEntities();
		
		// filter entities
		pool.filterEntities(config.getEntityFilters());
		
		return pool;
	}

	/**
	 * Sets the pool's snapshot. This is where referenced entities and passive properties are loaded from.
	 * 
	 * @param snapshot
	 */
	public void setSnapshot(GabotoSnapshot snapshot){
		this.snapshot = snapshot;
	}
	
	/**
	 * Sets a new configuration object.
	 * 
	 * @param config
	 */
	public void setConfig(GabotoEntityPoolConfiguration config){
		this.config = config;
	}
	
	
	/**
	 * Removes entities that do not fulfill all filter criteria.
	 * 
	 * <p>
	 * {@link GabotoEntity}s in this pool might still hold references to removed
	 * entities after filtering. Filtered entities are only removed from the pool, not
	 * from other entities in the pool.
	 * </p>
	 *  
	 * @param filters The collection of {@link EntityFilter}s.
	 */
	public void filterEntities(Collection<EntityFilter> filters){
		Collection<GabotoEntity> entitiesToRemove = new HashSet<GabotoEntity>();
		for(EntityFilter filter : filters){
			for(GabotoEntity entity : this.getEntities()){
				try{
					filter.appliesTo().cast(entity);
					if( ! filter.filterEntity(entity))
						entitiesToRemove.add(entity);
				} catch(ClassCastException e){}
			}
		}
		for(GabotoEntity entity : entitiesToRemove)
			this.removeEntity(entity);
	}
	
	public void addMissingReferencesForEntity(Collection<Resource> resources, Map<String, Collection<EntityExistsCallback>> callbacks) {
		if(this.snapshot == null){
			logger.debug("Cannot load referenced entities if no snapshot is provided.");
			return;
		}
		
		// copy resource collection and map
		Collection<Resource> myResources = new HashSet<Resource>();
		myResources.addAll(resources);
		
		// add entity?
		boolean direct = config != null && config.isAddReferencedEntitiesToPool();
		
		// loop over resources
		for(Resource res : myResources){
			// do we have the entity already loaded
			String uri = res.getURI();
			GabotoEntity entity = null;
			if(entities.containsKey(uri))
				entity = entities.get(uri);
			else if(referencedEntities.containsKey(uri))
				entity = referencedEntities.get(uri);
			
			if(null == entity){
				if(! snapshot.containsResource(res))
					continue;

				try{
					entity = this.addEntity(res, snapshot, direct, true);
				} catch(EntityClassNotFoundException e){
					logger.info(e.getMessage());
				} catch (ResourceDoesNotExistException e) {
					e.printStackTrace();
				} catch (EntityDoesNotExistException e) {
					e.printStackTrace();
				}
			}
			
			// callback
			if(null != entity){
				Collection<EntityExistsCallback> callbacksForEntity = callbacks.get(entity.getUri());
				// copy
				Collection<EntityExistsCallback> myCallbacks = new HashSet<EntityExistsCallback>();
				myCallbacks.addAll(callbacksForEntity);
				for(EntityExistsCallback callback : myCallbacks)
					callback.entityExists(this, entity);
			}
		}
	}
	
	public void addPassiveEntities() {
		for(GabotoEntity entity : entities.values())
			addPassiveEntitiesFor(entity);
	}
	
	public void addPassiveEntitiesFor(GabotoEntity entity) {
		if(this.snapshot == null){
			logger.debug("Cannot load passive entities if no snapshot is provided.");
			return;
		}
		
		Model model = snapshot.getModel();
		Graph graph = model.getGraph();
		
		// tell entity that its passive things are to be loaded
		entity.setPassiveEntitiesLoaded();
		
		// load passive entities
		Collection<PassiveEntitiesRequest> requests = entity.getPassiveEntitiesRequest();
		if(null == requests)
			return;
		
		// add stuff direct?
		final boolean direct = config != null && config.isAddReferencedEntitiesToPool();
		
		// loop over requests for entity
		for(final PassiveEntitiesRequest request : requests){
			
			if(request.getCollectionType() == PASSIVE_PROPERTY_COLLECTION_TYPE_NONE){
				ExtendedIterator it = graph.find(Node.ANY, Node.createURI(request.getUri()), Node.createURI(entity.getUri()));
				while(it.hasNext()){
					Triple t = (Triple) it.next();
					if(! t.getSubject().isURI())
						throw new CorruptDataException("The node should really be a uri!");
					
					String nodesURI = t.getSubject().getURI();
					if(entities.containsKey(nodesURI))
						request.passiveEntityLoaded(entities.get(nodesURI));
					else if(referencedEntities.containsKey(nodesURI))
						request.passiveEntityLoaded(referencedEntities.get(nodesURI));
					else {
						Resource res = snapshot.getResource(nodesURI);
						addEntity(res, snapshot, direct, true);
							
						request.passiveEntityLoaded(referencedEntities.get(nodesURI));
					}
				}
			} else if(request.getCollectionType() == PASSIVE_PROPERTY_COLLECTION_TYPE_BAG){
				// find bag we are in
			    String query = GabotoPredefinedQueries.getStandardPrefixes();
				query += "SELECT ?res WHERE {\n" +
								"?res <" + request.getUri() + "> ?bag .\n" +
								"?bag rdf:type <http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag> .\n" +
								"?bag ?li <" + entity.getUri() + "> .\n" +
								"}";

				// execute query
				snapshot.execSPARQLSelect(query, new QuerySolutionProcessor(){
					public void processSolution(QuerySolution solution) {
						Resource res = solution.getResource("res");
						if(null != res){
							if(entities.containsKey(res.getURI()))
								request.passiveEntityLoaded(entities.get(res.getURI()));
							else if(referencedEntities.containsKey(res.getURI()))
								request.passiveEntityLoaded(referencedEntities.get(res.getURI()));
							else {
								try {
									addEntity(res, snapshot, direct, true);
									
									request.passiveEntityLoaded(referencedEntities.get(res.getURI()));
								} catch (ResourceDoesNotExistException e) {
									e.printStackTrace();
								} catch (EntityClassNotFoundException e) {
									e.printStackTrace();
								} catch (EntityDoesNotExistException e) {
									e.printStackTrace();
								}
							}
						}
					}

					public boolean stopProcessing() {
						return true;
					}
					
				});
			}
		}
	}
	
	
	public GabotoEntity addEntity(Resource res, GabotoSnapshot snapshot) throws EntityClassNotFoundException, CorruptDataException, ResourceDoesNotExistException, EntityDoesNotExistException {
		return addEntity(res, snapshot, true, false);
	}
	
	/**
	 * Creates an entity from a resource and adds it to this pool.
	 * 
	 * 
	 * @param res The source the entity is created from
	 * @param snapshot The snapshot the resource was taken from.
	 * @param direct 
	 * 
	 * @throws EntityClassNotFoundException 
	 * @throws ResourceDoesNotExistException 
	 * @throws EntityDoesNotExistException 
	 */
	private GabotoEntity addEntity(Resource res, GabotoSnapshot snapshot, boolean direct, boolean bypassTests) throws EntityClassNotFoundException, ResourceDoesNotExistException, EntityDoesNotExistException {
		if(! snapshot.containsResource(res))
			throw new ResourceDoesNotExistException(res);
		
		// find type
		String type = null;
		
		Statement typeStmt = res.getProperty(RDF.type);
		
		if(typeStmt == null)
			type = gaboto.getTypeOf(res.getURI());
		
		if(type == null && (typeStmt == null || ! typeStmt.getObject().isResource())){
			logger.debug("Found an untyped resource: " + res.getURI());
			return null;
		} else if(type == null && typeStmt != null) 		
			type = ((Resource)typeStmt.getObject()).getURI();
		
		//// try to load entity class
		try {
			Class<?> entityClass = GabotoOntologyLookup.getEntityClassFor(type);
			
			// instantiate
			GabotoEntity entity = (GabotoEntity) entityClass.newInstance();
			
			// test type
			// is entity of allowed type or is it an indirect add
			if(! bypassTests){
				if(direct && null != config && 
				   ! config.getAcceptedTypes().isEmpty() && 
				   ! config.getAcceptedTypes().contains(entity.getType())){
					   return null;
				}
				// is entity of an unaccepted type? 
				if( direct && null != config &&
					! config.getUnacceptedTypes().isEmpty() && 
				      config.getUnacceptedTypes().contains(entity.getType())){
					    return null;
				}
			}
			
			// resource filters
			if(direct){
				// resource filters
				boolean passedFilter = true;
				for(ResourceFilter filter : config.getResourceFilters()){
					try{
						filter.appliesTo().cast(entity);
						if( ! filter.filterResource(res)){
							passedFilter = false;
							break;
						}
					} catch(ClassCastException e){}
				}
				if(!passedFilter)
					return null;
			}
			
			// load entity
			entity.loadFromSnapshot(res, snapshot, this);
			
			// add entity
			return this.addEntity(entity, direct);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Removes an entity from this pool.
	 * 
	 * <p>
	 * The entity will only be removed from the pool. Even if any other entity in this pool holds
	 * a reference to the deleted entity, that reference will not be deleted.
	 * </p>
	 * 
	 * @param entity The entity that is to be deleted.
	 */
	public void removeEntity(GabotoEntity entity){
		entities.remove(entity.getUri());
	}
	
	/**
	 * Adds an entity to this pool.
	 * 
	 * @param entity The entity that is to be added.
	 */
	public GabotoEntity addEntity(GabotoEntity entity){
		return addEntity(entity, true);
	}
	
	
	/**
	 * Adds an entity to this pool and allows for bypassing defined filters.
	 * 
	 * @param entity The entity that is to be added.
	 * @param bypassFilters Whether or not to bypass defined filters.
	 * @param direct 
	 */
	private GabotoEntity addEntity(GabotoEntity entity, boolean direct){
		// add entity to list of directly added entities
		if(direct){
			if(referencedEntities.containsKey(entity.getUri()))
				referencedEntities.remove(entity.getUri());
			directEntities.add(entity.getUri());
		}
		
		// store information
		entity.setCreatedFromInformation(this);
		
		// add entity
		if( direct || (null != config && config.isAddReferencedEntitiesToPool()))
			entities.put(entity.getUri(), entity);
		else
			referencedEntities.put(entity.getUri(), entity);
		
		// lazy initialization?
		if(config != null && ! config.isEnableLazyDereferencing())
			entity.resolveDirectReferences();
		
		return entity;
	}
	
	/**
	 * Creates a jena {@link Model} from the entities in this pool.
	 * 
	 * @return A jena {@link Model} that contains all the information from the entities in this pool.
	 */
	public Model createJenaModel(){
		Model model = ModelFactory.createDefaultModel();

		for(GabotoEntity e : entities.values()){
			e.addToModel(model);
		}

		return model;
	}
	
	/**
	 * Creates a jena {@link Model} for the entities from this pool that are in the collection.
	 * 
	 * @param entities A list of entities that are to be transformed into a jena {@link Model}.
	 * @return A jena {@link Model}.
	 */
	public Model createJenaModel(Collection<GabotoEntity> entities){
		Model model = ModelFactory.createDefaultModel();

		for(GabotoEntity e : entities){
			if(this.entities.containsKey(e.getUri())){
				e.addToModel(model);
			}
		}

		return model;
	}
	
	/**
	 * Creates an GabotoSnapshot based on the information in this entity pool.
	 * @return An GabotoSnapshot based on the information in this entity pool.
	 */
	public GabotoSnapshot createSnapshot(){
		return new GabotoSnapshot(createJenaModel(), gaboto);
	}

	/**
	 * Returns the size of this entity pool.
	 * @return The number of entities in this pool.
	 */
	public int getSize(){
		return entities.size();
	}
	
	/**
	 * Returns an {@link GabotoEntity} or null if the entity is not in this pool.
	 * 
	 * @param uri The entity's URI.
	 * 
	 * @return The entity or null.
	 */
	public GabotoEntity getEntity(String uri){
		return entities.get(uri);
	}
	
	/**
	 * Returns whether or not this pool contains the specified entity.
	 *  
	 * @param uri The entity's URI.
	 * 
	 * @return Whether or not this pool contains the specified entity.
	 */
	public boolean containsEntity(String uri){
		return entities.containsKey(uri);
	}
	
	/**
	 * Returns all the entities registered in this pool.
	 * 
	 * @return This pool's entities.
	 */
	public Collection<GabotoEntity> getEntities(){
		return entities.values();
	}
	
	/**
	 * Returns all the entities registered in this pool.
	 * 
	 * @return This pool's entities.
	 */
	public List<GabotoEntity> getEntitiesSorted(final String propertyURI){
		List<GabotoEntity> list = new ArrayList<GabotoEntity>();
		list.addAll(entities.values());
		
		Collections.sort(list, new Comparator<GabotoEntity>(){

			public int compare(GabotoEntity o1, GabotoEntity o2) {
				Object value1 = o1.getPropertyValue(propertyURI);
				Object value2 = o2.getPropertyValue(propertyURI);
				
				// try to cast as strings
				try{
					String s1 = (String) value1;
					String s2 = (String) value2;
					
					if(null != s1 && s2 == null)
						return -1;
					if(null == s1 && s2 != null)
						return 1;
					if(null == s1 && null == s2)
						return 0;
					
					return s1.compareTo(s2);
				} catch(ClassCastException e){
					throw new IllegalArgumentException("Property has to be a simple literal property.");
				}
			}
		});
		
		return list;
	}
	
	/**
	 * Returns all the entities registered in this pool filtered by type.
	 * @param entityType The GabotoEntity Class describing the entity
	 * @return A filtered set of entities.
	 */
	@SuppressWarnings("unchecked")
	public <T extends GabotoEntity> Collection<T> getEntities(T entityType){
		Set<T> filteredEntities = new HashSet<T>();
		
		for(GabotoEntity e : entities.values()){
			try{
				T castedEntity = (T) entityType.getClass().cast(e);
				filteredEntities.add(castedEntity);
			} catch(ClassCastException ex){}
		}
		
		return filteredEntities;
	}
	
}
