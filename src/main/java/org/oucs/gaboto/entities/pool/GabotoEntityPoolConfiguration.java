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

import java.util.Collection;
import java.util.HashSet;

import org.oucs.gaboto.entities.pool.filters.EntityFilter;
import org.oucs.gaboto.entities.pool.filters.ResourceFilter;
import org.oucs.gaboto.exceptions.EntityPoolInvalidConfigurationException;
import org.oucs.gaboto.model.Gaboto;
import org.oucs.gaboto.model.GabotoSnapshot;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Used to configure the creation of {@link GabotoEntityPool}s.
 * 
 * <p>
 * An {@link GabotoEntityPool} can either be created from an
 * {@link GabotoSnapshot} or from a Jena {@link Model} and an {@link Gaboto}
 * object. This can be seen as the source from which the pool is created. All
 * other options define which entities from the source are to be added to the
 * pool.
 * </p>
 * 
 * @author Arno Mittelbach
 * @version 0.1
 * @see GabotoEntityPool
 */
public class GabotoEntityPoolConfiguration {

  private Model model = null;

  private GabotoSnapshot snapshot = null;

  private Gaboto gaboto = null;

  private boolean enableLazyDereferencing = true;

  private Collection<EntityFilter> entityFilters = new HashSet<EntityFilter>();

  private Collection<ResourceFilter> resourceFilters = new HashSet<ResourceFilter>();

  private boolean useResourceCollection = false;

  private boolean addReferencedEntitiesToPool = false;

  private boolean createPassiveEntities = false;

  private Collection<Resource> resources = new HashSet<Resource>();

  private Collection<String> acceptedTypes = new HashSet<String>();

  private Collection<String> unacceptedTypes = new HashSet<String>();

  /**
   * @param snapshot
   */
  public GabotoEntityPoolConfiguration(GabotoSnapshot snapshot) {
    super();
    this.snapshot = snapshot;
  }

  /**
   * @param gaboto
   * @param model
   */
  public GabotoEntityPoolConfiguration(Gaboto gaboto, Model model) {
    super();
    this.gaboto = gaboto;
    this.model = model;
  }

  /**
   * Tests whether this configuration can be used to create an
   * {@link GabotoEntityPool}.
   * 
   * @throws EntityPoolInvalidConfigurationException
   * @throws EntityPoolAmbiguousConfigurationException
   */
  public void testConfiguration()
      throws EntityPoolInvalidConfigurationException,
      EntityPoolAmbiguousConfigurationException {
    if (null == snapshot && (null == model || null == gaboto))
      throw new EntityPoolInvalidConfigurationException(
          "Either a snapshot or a jena and gaboto model have to be supplied.");

    if (null != snapshot && (null != model || null != gaboto))
      throw new EntityPoolAmbiguousConfigurationException(
          "Supplying a snapshot and a jena model/gaboto model is ambiguous.");
  }

  /**
   * Adds another {@link EntityFilter} to the configuration.
   * 
   * <p>
   * {@link EntityFilter}s are used to filter the entities that are added to the
   * pool. For a more detailed description see {@link EntityFilter}.
   * </p>
   * 
   * @see EntityFilter
   * @param filter
   *          The filter to be added.
   */
  public void addEntityFilter(EntityFilter filter) {
    this.entityFilters.add(filter);
  }

  /**
   * Returns a collection of {@link EntityFilter}s.
   * 
   * @return All {@link EntityFilter}s.
   */
  public Collection<EntityFilter> getEntityFilters() {
    return entityFilters;
  }

  /**
   * Sets the entity filters to the provided collection.
   * 
   * @param filters
   *          The entity filters to use.
   */
  public void setEntityFilters(Collection<EntityFilter> filters) {
    this.entityFilters = filters;
  }

  /**
   * Empties the collection of entity filters.
   */
  public void clearEntityFilters() {
    this.entityFilters.clear();
  }

  /**
   * Adds another {@link ResourceFilter} to the configuration.
   * 
   * <p>
   * {@link ResourceFilter}s are used to filter the entities that are added to
   * the pool. For a more detailed description see {@link ResourceFilter}.
   * </p>
   * 
   * @see ResourceFilter
   * @param filter
   *          The filter to be added.
   */
  public void addResourceFilter(ResourceFilter filter) {
    this.resourceFilters.add(filter);
  }

  /**
   * Returns a collection of {@link ResourceFilter}s.
   * 
   * @return All {@link ResourceFilter}s.
   */
  public Collection<ResourceFilter> getResourceFilters() {
    return resourceFilters;
  }

  /**
   * Sets the resource filters to the provided collection.
   * 
   * @param filters
   *          The resource filters to use.
   */
  public void setResourceFilters(Collection<ResourceFilter> filters) {
    this.resourceFilters = filters;
  }

  /**
   * Empties the collection of resource filters.
   */
  public void clearResourceFilters() {
    this.resourceFilters.clear();
  }

  /**
   * Returns null or the Jena Model that is to be used.
   * 
   * @return The model to be used or null.
   */
  public Model getModel() {
    return model;
  }

  /**
   * Sets the Jena Model that is to be used in the creation of the entity pool.
   * 
   * @param model
   *          The model to be used.
   */
  public void setModel(Model model) {
    this.model = model;
  }

  /**
   * Returns null or the snapshot that is to be used to create the entity pool.
   * 
   * @return null or the snapshot.
   */
  public GabotoSnapshot getSnapshot() {
    return snapshot;
  }

  /**
   * Sets the snapshot that is to be used as the source for the creation of the
   * entity pool.
   * 
   * @param snapshot
   *          The snapshot to be used as source.
   */
  public void setSnapshot(GabotoSnapshot snapshot) {
    this.snapshot = snapshot;
  }

  /**
   * Returns null or the Gaboto model that is to be used in the creation of the
   * entity pool.
   * 
   * @return The Gaboto model that is to be used in the creation of the entity
   *         pool.
   */
  public Gaboto getGaboto() {
    return gaboto;
  }

  /**
   * Sets the Gaboto model that is to be used in the creation of the entity
   * pool.
   * 
   * @param gaboto
   *          the Gaboto model that is to be used in the creation of the entity
   *          pool.
   */
  public void setGaboto(Gaboto gaboto) {
    this.gaboto = gaboto;
  }

  /**
   * Returns the list of resources that are to be created.
   * 
   * @return the list of resources that are to be created.
   * @see #useResourceCollection
   */
  public Collection<Resource> getResources() {
    return resources;
  }

  /**
   * Sets the list of resources that are to be created.
   * 
   * <p>
   * Sets the useResource flag (see {@link #setUseResourceCollection(boolean)}).
   * </p>
   * 
   * @param resources
   *          The list of resources that are to be created
   * @see #useResourceCollection
   */
  public void setResources(Collection<Resource> resources) {
    this.resources = resources;
    this.setUseResourceCollection(true);
  }

  /**
   * Adds a resource to the list of resources that are to be created.
   * 
   * <p>
   * Sets the useResource flag (see {@link #setUseResourceCollection(boolean)}).
   * </p>
   * 
   * @param resource
   *          The resource to be added to the list of resources that are to be
   *          created.
   * @see #useResourceCollection
   */
  public void addResource(Resource resource) {
    this.resources.add(resource);
    this.setUseResourceCollection(true);
  }

  /**
   * Returns whether or not referenced entities that were created are added to
   * the pool.
   * 
   * <p>
   * Default is true.
   * </p>
   * 
   * @return Whether or not referenced entities that were created are added to
   *         the pool.
   */
  public boolean isAddReferencedEntitiesToPool() {
    return addReferencedEntitiesToPool;
  }

  /**
   * Set whether or not referenced entities that were created are added to the
   * pool.
   * 
   * <p>
   * Default is false.
   * </p>
   * 
   * @param addReferencedEntitiesToPool
   *          Whether or not referenced entities that were created are added to
   *          the pool.
   */
  public void setAddReferencedEntitiesToPool(boolean addReferencedEntitiesToPool) {
    this.addReferencedEntitiesToPool = addReferencedEntitiesToPool;
  }

  /**
   * Returns which types are to be created.
   * 
   * <p>
   * If the list is empty, all types are accepted.
   * </p>
   * 
   * @return A collection of accepted types.
   */
  public Collection<String> getAcceptedTypes() {
    return acceptedTypes;
  }

  /**
   * Sets which types are to be created.
   * 
   * <p>
   * If the list is empty, all types are accepted.
   * </p>
   * 
   * @param acceptedTypes
   *          A collection of accepted types.
   */
  public void setAcceptedTypes(Collection<String> acceptedTypes) {
    this.acceptedTypes = acceptedTypes;
  }

  /**
   * Adds another type to the list of accepted types.
   * 
   * @see #setAcceptedTypes(Collection)
   * 
   * @param type
   *          The type to be added.
   */
  public void addAcceptedType(String type) {
    acceptedTypes.add(type);
  }

  /**
   * Returns which types are not to be created.
   * 
   * <p>
   * If the list is empty, all types are accepted.
   * </p>
   * 
   * @return A collection of not accepted types.
   */
  public Collection<String> getUnacceptedTypes() {
    return unacceptedTypes;
  }

  /**
   * Sets which types are not to be created.
   * 
   * <p>
   * If the list is empty, all types are accepted.
   * </p>
   * 
   * @param acceptedTypes
   *          A collection of unaccepted types.
   */
  public void setUnacceptedTypes(Collection<String> unacceptedTypes) {
    this.unacceptedTypes = unacceptedTypes;
  }

  /**
   * Adds another type to the list of unaccepted types.
   * 
   * @see #setAcceptedTypes(Collection)
   * 
   * @param type
   *          The type to be added.
   */
  public void addUnacceptedType(String type) {
    unacceptedTypes.add(type);
  }

  /**
   * Tells the pool to use the resource collection.
   * 
   * @param useResourceCollection
   * @see #setResources(Collection)
   * @see #addResource(Resource)
   */
  public void setUseResourceCollection(boolean useResourceCollection) {
    this.useResourceCollection = useResourceCollection;
  }

  /**
   * Returns whether or not the pool uses the resource collection.
   * 
   * @return Whether or not the pool uses the resource collection.
   * @see #setResources(Collection)
   * @see #addResource(Resource)
   */
  public boolean isUseResourceCollection() {
    return useResourceCollection;
  }

  /**
   * Defines whether or not passive properties are to be resolved.
   * 
   * @param createPassiveEntities
   */
  public void setCreatePassiveEntities(boolean createPassiveEntities) {
    this.createPassiveEntities = createPassiveEntities;
  }

  public boolean isCreatePassiveEntities() {
    return createPassiveEntities;
  }

  /**
   * @param enableLazyDereferencing
   *          the enableLazyDereferencing to set
   */
  public void setEnableLazyDereferencing(boolean enableLazyDereferencing) {
    this.enableLazyDereferencing = enableLazyDereferencing;
  }

  /**
   * @return the enableLazyDereferencing
   */
  public boolean isEnableLazyDereferencing() {
    return enableLazyDereferencing;
  }

}
