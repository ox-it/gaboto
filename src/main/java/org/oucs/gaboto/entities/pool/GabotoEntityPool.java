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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.oucs.gaboto.GabotoRuntimeException;
import org.oucs.gaboto.entities.GabotoEntity;
import org.oucs.gaboto.entities.pool.filters.EntityFilter;
import org.oucs.gaboto.entities.pool.filters.ResourceFilter;
import org.oucs.gaboto.model.EntityDoesNotExistException;
import org.oucs.gaboto.model.Gaboto;
import org.oucs.gaboto.model.GabotoSnapshot;
import org.oucs.gaboto.model.IncoherenceException;
import org.oucs.gaboto.model.ResourceDoesNotExistException;
import org.oucs.gaboto.model.SPARQLQuerySolutionProcessor;
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
 * A collection of {@link GabotoEntity}s.
 * 
 * <p>
 * An {@link GabotoEntityPool} can be seen as a collection of
 * {@link GabotoEntity}s that have something in common. {@link GabotoEntityPool}s 
 * can be created by simply adding entities "by hand" or they can be
 * automatically created from an {@link GabotoSnapshot} or a Jena {@link Model}.
 * </p>
 * 
 * <p>
 * The automatic creation is configured using a
 * {@link GabotoEntityPoolConfiguration} object.
 * </p>
 * 
 * @author Arno Mittelbach
 * @version 0.1
 * 
 * @see GabotoEntityPoolConfiguration
 */
public class GabotoEntityPool implements Collection<GabotoEntity> {

  private static Logger logger = Logger.getLogger(GabotoEntityPool.class
      .getName());

  public static final int PASSIVE_PROPERTY_COLLECTION_TYPE_NONE = 1;
  public static final int PASSIVE_PROPERTY_COLLECTION_TYPE_BAG = 2;

  Map<String, GabotoEntity> entityMap = new HashMap<String, GabotoEntity>();
  Map<String, GabotoEntity> referencedEntityMap = new HashMap<String, GabotoEntity>();

  private HashSet<String> directEntities = new HashSet<String>();

  private GabotoEntityPoolConfiguration poolConfig;

  private Gaboto gaboto;

  GabotoSnapshot snapshot;

  /**
   * Creates a new, empty entity pool.
   * 
   * <p>
   * If you want to be able to load passive properties, supply the snapshot you
   * are working with.
   * </p>
   * 
   * @see #GabotoEntityPool(Gaboto, GabotoSnapshot)
   */
  public GabotoEntityPool(Gaboto gaboto) {
    this.gaboto = gaboto;
  }

  public GabotoEntityPool(Gaboto gaboto, GabotoSnapshot snapshot) {
    this.gaboto = gaboto;
    this.snapshot = snapshot;
  }

  public GabotoEntityPool(GabotoSnapshot snapshot) {
    this.gaboto = snapshot.getGaboto();
    this.snapshot = snapshot;
  }

  /**
   * Creates a new, empty entity pool with a given configuration.
   * 
   * <p>
   * The configuration can be used to define {@link EntityFilter}s.
   * </p>
   * 
   * @param config
   *          The configuration.
   */
  public GabotoEntityPool(GabotoEntityPoolConfiguration config) {
    this.gaboto = config.getGaboto();
    this.poolConfig = config;
  }

  /**
   * Creates a new entity pool from a given configuration.
   * 
   * @param config
   *          The configuration
   * @return a new entity pool created from a given configuration.
   * 
   */
  public static GabotoEntityPool createFrom(GabotoEntityPoolConfiguration config) {
    config.assertConfigurationValid();

    // get snapshot
    GabotoSnapshot snapshot = config.getSnapshot();
    if (snapshot == null)
      snapshot = new GabotoSnapshot(config.getModel(), config.getGaboto());

    if (config.isUseResourceCollection())
      return createFrom(config, snapshot, config.getResources());

    return createFrom(config, snapshot);
  }

  /**
   * Creates a new entity pool from a given snapshot.
   * 
   * @param snapshot
   *          snapshot to use
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
  private static GabotoEntityPool createFrom(
      GabotoEntityPoolConfiguration config, GabotoSnapshot snapshot) {
    GabotoEntityPool pool = new GabotoEntityPool(snapshot.getGaboto());
    pool.poolConfig = config;
    pool.snapshot = snapshot;

    Model model = snapshot.getModel();

    logger
        .debug("Attempting to create entity pool from model. This involves a lot of inflection and is error prone.");

    // get all types
    Collection<String> registeredTypes = snapshot.getGaboto().getOntologyLookup()
        .getRegisteredClassesAsURIs();
    for (String type : registeredTypes) {
      if (!config.getAcceptedTypes().isEmpty()
          && !config.getAcceptedTypes().contains(type))
        continue;

      if (!config.getUnacceptedTypes().isEmpty()
          && config.getUnacceptedTypes().contains(type))
        continue;

      try {
        logger.debug("Loading type " + type);
        Class<?> entityClass = snapshot.getGaboto().getOntologyLookup().getEntityClassFor(type);

        // get everything in the model of that type
        // TODO Be careful here if we have an inferencing model

        ResIterator it = model.listSubjectsWithProperty(RDF.type, snapshot
            .getProperty(type));
        while (it.hasNext()) {
          Resource res = it.nextResource();

          // instantiate
          GabotoEntity entity = (GabotoEntity) entityClass.newInstance();

          // resource filters
          boolean passedFilter = true;
          for (ResourceFilter filter : config.getResourceFilters()) {
            try {
              filter.appliesTo().cast(entity);
              if (!filter.filterResource(res)) {
                passedFilter = false;
                break;
              }
            } catch (ClassCastException e) {
              // On to the next one
            }
          }
          if (!passedFilter)
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

    if (config.isCreatePassiveEntities())
      pool.addPassiveEntities();

    // filter entities
    pool.filterEntities(config.getEntityFilters());

    return pool;
  }

  /**
   * Creates an entity pool from a snapshot with all the resources that are
   * specified in the supplied collection.
   * 
   * @param config
   * @param snapshot
   * @param resources
   * @return The created entity pool.
   */
  private static GabotoEntityPool createFrom(
      GabotoEntityPoolConfiguration config, GabotoSnapshot snapshot,
      Collection<Resource> resources) {
    GabotoEntityPool pool = new GabotoEntityPool(snapshot.getGaboto());
    pool.poolConfig = config;
    pool.snapshot = snapshot;

    logger
        .debug("Attempting to create pool from model and a set of resources.");

    // loop over resources
    for (Resource res : resources) {
      try {
        pool.addEntity(res, snapshot);
      } catch (Exception e) {
        throw new GabotoRuntimeException(e);
      }
    }

    //
    if (config.isCreatePassiveEntities())
      pool.addPassiveEntities();

    // filter entities
    pool.filterEntities(config.getEntityFilters());

    return pool;
  }

  /**
   * Sets the pool's snapshot. This is where referenced entities and passive
   * properties are loaded from.
   * 
   * @param snapshot
   */
  public void setSnapshot(GabotoSnapshot snapshot) {
    this.snapshot = snapshot;
  }

  /**
   * Sets a new configuration object.
   * 
   * @param config
   */
  public void setConfig(GabotoEntityPoolConfiguration config) {
    this.poolConfig = config;
  }

  /**
   * Removes entities that do not fulfill all filter criteria.
   * 
   * <p>
   * {@link GabotoEntity}s in this pool might still hold references to removed
   * entities after filtering. Filtered entities are only removed from the pool,
   * not from other entities in the pool.
   * </p>
   * 
   * @param filters
   *          The collection of {@link EntityFilter}s.
   */
  public void filterEntities(Collection<EntityFilter> filters) {
    Collection<GabotoEntity> entitiesToRemove = new HashSet<GabotoEntity>();
    for (EntityFilter filter : filters) {
      for (GabotoEntity entity : this.getEntities()) {
        try {
          filter.appliesTo().cast(entity);
          if (!filter.filterEntity(entity))
            entitiesToRemove.add(entity);
        } catch (ClassCastException e) {
          // On to the next one
        }
      }
    }
    for (GabotoEntity entity : entitiesToRemove)
      this.removeEntity(entity);
  }

  public void addMissingReferencesForEntity(Collection<Resource> resources,
      Map<String, Collection<EntityExistsCallback>> callbacks) {
    if (this.snapshot == null) {
      throw new GabotoRuntimeException("Cannot load referenced entities as snapshot is null.");
    }

    // copy resource collection and map
    Collection<Resource> myResources = new HashSet<Resource>();
    myResources.addAll(resources);

    // add entity?
    boolean direct = poolConfig != null && poolConfig.isAddReferencedEntitiesToPool();

    // loop over resources
    for (Resource res : myResources) {
      // do we have the entity already loaded
      String uri = res.getURI();
      GabotoEntity entity = null;
      if (entityMap.containsKey(uri))
        entity = entityMap.get(uri);
      else if (referencedEntityMap.containsKey(uri))
        entity = referencedEntityMap.get(uri);

      if (entity == null) {
        if (!snapshot.containsResource(res))
          continue;
        try {
          entity = this.addEntity(res, snapshot, direct, true);
        } catch (ResourceDoesNotExistException e) {
          throw new IncoherenceException(e);
        }
      }

      if (entity != null) {
        Collection<EntityExistsCallback> callbacksForEntity = callbacks.get(entity.getUri());
        // copy
        Collection<EntityExistsCallback> myCallbacks = new HashSet<EntityExistsCallback>();
        myCallbacks.addAll(callbacksForEntity);
        for (EntityExistsCallback callback : myCallbacks)
          callback.entityExists(this, entity);
      }
    }
  }

  public void addPassiveEntities() {
    for (GabotoEntity entity : entityMap.values())
      addPassiveEntitiesFor(entity);
  }

  public void addPassiveEntitiesFor(GabotoEntity entity) throws EntityDoesNotExistException {
    if (this.snapshot == null) 
      throw new GabotoRuntimeException("Cannot load passive entities as snapshot is null.");

    Model model = snapshot.getModel();
    Graph graph = model.getGraph();

    // tell entity that its passive things have been loaded
    entity.setPassiveEntitiesLoaded();

    // load passive entities
    Collection<PassiveEntitiesRequest> requests = entity.getPassiveEntitiesRequest();
    if (requests == null)
      return;

    // add stuff direct?
    final boolean direct = poolConfig != null
        && poolConfig.isAddReferencedEntitiesToPool();

    // loop over requests for entity
    for (final PassiveEntitiesRequest request : requests) {

      if (request.getCollectionType() == PASSIVE_PROPERTY_COLLECTION_TYPE_NONE) {
        ExtendedIterator it = graph.find(Node.ANY, Node.createURI(request
            .getUri()), Node.createURI(entity.getUri()));
        while (it.hasNext()) {
          Triple t = (Triple) it.next();
          if (!t.getSubject().isURI())
            throw new IncoherenceException("The node should really be a uri!");

          String nodesURI = t.getSubject().getURI();
          if (entityMap.containsKey(nodesURI))
            request.passiveEntityLoaded(entityMap.get(nodesURI));
          else if (referencedEntityMap.containsKey(nodesURI))
            request.passiveEntityLoaded(referencedEntityMap.get(nodesURI));
          else {
            try {
              addEntity(snapshot.getResource(nodesURI), snapshot, direct, true);
            } catch (ResourceDoesNotExistException e) {
              throw new IncoherenceException(nodesURI, e);
            }

            request.passiveEntityLoaded(referencedEntityMap.get(nodesURI));
          }
        }
      } else if (request.getCollectionType() == PASSIVE_PROPERTY_COLLECTION_TYPE_BAG) {
        // find bag we are in
        String query = GabotoPredefinedQueries.getStandardPrefixes();
        query += "SELECT ?res WHERE {\n"
            + "?res <"
            + request.getUri()
            + "> ?bag .\n"
            + "?bag rdf:type <http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag> .\n"
            + "?bag ?li <" + entity.getUri() + "> .\n" + "}";

        // execute query
        snapshot.execSPARQLSelect(query, new SPARQLQuerySolutionProcessor() {
          public void processSolution(QuerySolution solution) {
            Resource res = solution.getResource("res");
            if (res != null) {
              if (entityMap.containsKey(res.getURI()))
                request.passiveEntityLoaded(entityMap.get(res.getURI()));
              else if (referencedEntityMap.containsKey(res.getURI()))
                request.passiveEntityLoaded(referencedEntityMap
                    .get(res.getURI()));
              else {
                try {
                  addEntity(res, snapshot, direct, true);

                  request.passiveEntityLoaded(referencedEntityMap.get(res
                      .getURI()));
                } catch (Exception e) {
                  throw new GabotoRuntimeException(e);
                }
              }
            }
          }

          public boolean stopProcessing() {
            return false;
          }

        });
      }
    }
  }

  public GabotoEntity addEntity(Resource res, GabotoSnapshot snapshotP) throws ResourceDoesNotExistException {
    return addEntity(res, snapshotP, true, false);
  }

  /**
   * Creates an entity from a resource and adds it to this pool.
   * 
   * 
   * @param resource
   *          The source the entity is created from
   * @param snapshotFrom
   *          The snapshot the resource was taken from.
   * @param direct
   * @param bypassTests whether to check entity validity
   */
  GabotoEntity addEntity(Resource resource, GabotoSnapshot snapshotFrom,
      boolean direct, boolean bypassTests) throws ResourceDoesNotExistException  {
    if (!snapshotFrom.containsResource(resource))
      throw new ResourceDoesNotExistException(resource);

    // find type
    String type = null;

    Statement typeStmt = resource.getProperty(RDF.type);

    if (typeStmt == null)
      type = gaboto.getTypeOf(resource.getURI());

    if (type == null
        && (typeStmt == null || !typeStmt.getObject().isResource())) {
      logger.debug("Found an untyped resource: " + resource.getURI());
      return null;
    } else if (type == null && typeStmt != null)
      type = ((Resource) typeStmt.getObject()).getURI();
    
    Class<?> entityClass = snapshot.getGaboto().getOntologyLookup().getEntityClassFor(type);

      // instantiate
    GabotoEntity entity;
    try {
      entity = (GabotoEntity) entityClass.newInstance();
    } catch (Exception e) {
      throw new GabotoRuntimeException(e);
    }

    // test type
    // is entity of allowed type or is it an indirect add
    if (!bypassTests) {
      if (direct && poolConfig != null && !poolConfig.getAcceptedTypes().isEmpty()
          && !poolConfig.getAcceptedTypes().contains(entity.getType())) {
        return null;
      }
      // is entity of an unaccepted type?
      if (direct && null != poolConfig && !poolConfig.getUnacceptedTypes().isEmpty()
          && poolConfig.getUnacceptedTypes().contains(entity.getType())) {
        return null;
      }
    }

    // resource filters
    if (direct) {
      // resource filters
      boolean passedFilter = true;
      for (ResourceFilter filter : poolConfig.getResourceFilters()) {
        try {
          filter.appliesTo().cast(entity);
          if (!filter.filterResource(resource)) {
            passedFilter = false;
            break;
          }
        } catch (ClassCastException e) {
          // On to the next one
        }
      }
      if (!passedFilter)
        return null;
    }

    // load entity
    entity.loadFromSnapshot(resource, snapshotFrom, this);

    // add entity
    return this.addEntity(entity, direct);
  }

  /**
   * Removes an entity from this pool.
   * 
   * <p>
   * The entity will only be removed from the pool. Even if any other entity in
   * this pool holds a reference to the deleted entity, that reference will not
   * be deleted.
   * </p>
   * 
   * @param entity
   *          The entity that is to be deleted.
   */
  public void removeEntity(GabotoEntity entity) {
    entityMap.remove(entity.getUri());
  }

  /**
   * Adds an entity to this pool.
   * 
   * @param entity
   *          The entity that is to be added.
   */
  public GabotoEntity addEntity(GabotoEntity entity) {
    return addEntity(entity, true);
  }

  /**
   * Adds an entity to this pool and allows for bypassing defined filters.
   * 
   * @param entity
   *          The entity that is to be added.
   * @param bypassFilters
   *          Whether or not to bypass defined filters.
   * @param direct
   */
  private GabotoEntity addEntity(GabotoEntity entity, boolean direct) {
    // add entity to list of directly added entities
    if (direct) {
      if (referencedEntityMap.containsKey(entity.getUri()))
        referencedEntityMap.remove(entity.getUri());
      directEntities.add(entity.getUri());
    }

    entity.setCreatedFromPool(this);

    // add entity
    if (direct || (poolConfig != null && poolConfig.isAddReferencedEntitiesToPool()))
      entityMap.put(entity.getUri(), entity);
    else
      referencedEntityMap.put(entity.getUri(), entity);

    // lazy initialization?
    if (poolConfig != null && !poolConfig.isEnableLazyDereferencing())
      entity.resolveDirectReferences();

    return entity;
  }

  /**
   * Creates a jena {@link Model} from the entities in this pool.
   * 
   * @return A jena {@link Model} that contains all the information from the
   *         entities in this pool.
   */
  public Model createJenaModel() {
    Model model = ModelFactory.createDefaultModel();

    for (GabotoEntity e : entityMap.values()) {
      if (e.getUri() == null)
        throw new NullPointerException("No Uri set for " + e);
      e.addToModel(model);
    }

    return model;
  }

  /**
   * Creates a jena {@link Model} for the entities from this pool that are in
   * the collection.
   * 
   * @param entities
   *          A list of entities that are to be transformed into a jena
   *          {@link Model}.
   * @return A jena {@link Model}.
   */
  public Model createJenaModel(Collection<GabotoEntity> entities) {
    Model model = ModelFactory.createDefaultModel();

    for (GabotoEntity e : entities) {
      if (this.entityMap.containsKey(e.getUri())) {
        e.addToModel(model);
      }
    }

    return model;
  }

  /**
   * Creates an GabotoSnapshot based on the information in this entity pool.
   * 
   * @return An GabotoSnapshot based on the information in this entity pool.
   */
  public GabotoSnapshot createSnapshot() {
    return new GabotoSnapshot(createJenaModel(), gaboto);
  }

  /**
   * Returns an {@link GabotoEntity} or null if the entity is not in this pool.
   * 
   * @param uri
   *          The entity's URI.
   * 
   * @return The entity or null.
   */
  public GabotoEntity getEntity(String uri) {
    return entityMap.get(uri);
  }

  /**
   * Returns whether or not this pool contains the specified entity.
   * 
   * @param uri
   *          The entity's URI.
   * 
   * @return Whether or not this pool contains the specified entity.
   */
  public boolean containsEntity(String uri) {
    return entityMap.containsKey(uri);
  }

  /**
   * Returns all the entities registered in this pool.
   * 
   * @return This pool's entities.
   */
  public Collection<GabotoEntity> getEntities() {
    return entityMap.values();
  }

  /**
   * Returns all the entities registered in this pool.
   * 
   * @return This pool's entities.
   */
  public List<GabotoEntity> getEntitiesSorted(final String propertyURI) {
    List<GabotoEntity> list = new ArrayList<GabotoEntity>();
    list.addAll(entityMap.values());

    Collections.sort(list, new Comparator<GabotoEntity>() {

      public int compare(GabotoEntity o1, GabotoEntity o2) {
        Object value1 = o1.getPropertyValue(propertyURI);
        Object value2 = o2.getPropertyValue(propertyURI);

        // try to cast as strings
        try {
          String s1 = (String) value1;
          String s2 = (String) value2;

          if (null != s1 && s2 == null)
            return -1;
          if (null == s1 && s2 != null)
            return 1;
          if (null == s1 && null == s2)
            return 0;

          return s1.compareTo(s2);
        } catch (ClassCastException e) {
          throw new IllegalArgumentException(
              "Property has to be a simple literal property.");
        }
      }
    });

    return list;
  }

  /**
   * Returns all the entities registered in this pool filtered by type.
   * 
   * @param entityType
   *          The GabotoEntity Class describing the entity
   * @return A filtered set of entities.
   */
  @SuppressWarnings("unchecked")
  public <T extends GabotoEntity> Collection<T> getEntities(T entityType) {
    Set<T> filteredEntities = new HashSet<T>();

    for (GabotoEntity e : entityMap.values()) {
      try {
        T castedEntity = (T) entityType.getClass().cast(e);
        filteredEntities.add(castedEntity);
      } catch (ClassCastException ex) {
        // On to the next one
      }
    }

    return filteredEntities;
  }


  @Override
  public boolean add(GabotoEntity entity) {
    if (contains(entity))
      return false;
    else 
      addEntity(entity,true);
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends GabotoEntity> c) {
    boolean changed = false;
    for (GabotoEntity e : c) 
      if(add(e)) changed = true;
    return changed;
  }

  @Override
  public void clear() {
    entityMap = new HashMap<String, GabotoEntity>();
    referencedEntityMap = new HashMap<String, GabotoEntity>();
    directEntities = new HashSet<String>();
  }

  @Override
  public boolean contains(Object o) {
    return entityMap.containsValue(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    boolean contained = false;
    for (Object e : c) 
      if(contains(e)) contained = true;
    return contained;
  }

  @Override
  public boolean isEmpty() {
    return entityMap.isEmpty();
  }

  @Override
  public Iterator<GabotoEntity> iterator() {
    return entityMap.values().iterator();
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the size of this entity pool.
   * 
   * @return The number of entities in this pool.
   */
  public int getSize() {
    return entityMap.size();
  }

  @Override
  public int size() {
    return entityMap.size();
  }

  @Override
  public Object[] toArray() {
    return entityMap.values().toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return entityMap.values().toArray(a);
  }


}
