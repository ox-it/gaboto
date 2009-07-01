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
package org.oucs.gaboto.entities.time;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.oucs.gaboto.entities.GabotoEntity;
import org.oucs.gaboto.entities.utils.GabotoEntityUtils;
import org.oucs.gaboto.exceptions.EntityDoesNotExistException;
import org.oucs.gaboto.exceptions.GabotoRuntimeException;
import org.oucs.gaboto.exceptions.ResourceDoesNotExistException;
import org.oucs.gaboto.model.Gaboto;
import org.oucs.gaboto.model.GabotoSnapshot;
import org.oucs.gaboto.timedim.TimeInstant;
import org.oucs.gaboto.timedim.TimeSpan;
import org.oucs.gaboto.timedim.TimeUtils;
import org.oucs.gaboto.util.GabotoOntologyLookup;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.vocabulary.RDF;

import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
import de.fuberlin.wiwiss.ng4j.Quad;

/**
 * This class represents an GabotoEntity over time.
 * 
 * <p>
 * {@link GabotoEntity}s are representations of objects from the Gaboto ontology
 * at a specific point in time. In contrast {@link GabotoTimeBasedEntity}s are a
 * representation of an Gaboto object over time.
 * </p>
 * 
 * <p>
 * {@link GabotoTimeBasedEntity}s provide methods to set and change properties
 * that are time based. It also provides an iterator that returns a new
 * {@link GabotoEntity} whenever a property of the object has changed.
 * </p>
 * 
 * 
 * @author Arno Mittelbach
 * @version 0.1
 * @see GabotoEntity
 */
public class GabotoTimeBasedEntity implements Iterable<GabotoEntity> {

  private static Logger logger = Logger.getLogger(GabotoTimeBasedEntity.class.getName());

  /**
   * Stores the properties that are time bound.
   */
  private Map<TimeSpan, List<Property>> propertyMap = new HashMap<TimeSpan, List<Property>>();

  /**
   * Stores all properties that are valid throughout the lifespan of the entity.
   */
  private List<Property> universalProperties = new ArrayList<Property>();

  /**
   * Stores this entity's URI.
   */
  private String uri;

  /**
   * Stores this entity's lifespan.
   */
  private TimeSpan lifespan;

  /**
   * Stores the type of this entity
   */
  private String type;

  /**
   * Stores the entity class that this time based object is working with.
   */
  private Class<? extends GabotoEntity> entityClass;

  /**
   * Simple bean used to store relevant properties inside the
   * {@link GabotoTimeBasedEntity}.
   * 
   * @author Arno Mittelbach
   * @version 0.1
   */
  private class Property {
    private String uri;
    private Object value;

    /**
     * @param setter
     * @param getter
     * @param value
     */
    public Property(String uri, Object value) {
      super();
      this.uri = uri;
      this.value = value;
    }

    public String getPropertyUri() {
      return uri;
    }

    public void setUri(String uri) {
      this.uri = uri;
    }

    public Object getValue() {
      return value;
    }

    public void setValue(Object value) {
      this.value = value;
    }

  }

  /**
   * Creates a new empty GabotoTimeBasedEntity with an undefined lifespan (
   * {@link TimeUtils#EXISTANCE}).
   * 
   * <p>
   * Has the same result as calling
   * <code>new GabotoTimeBasedEntity(entityClass, uri, TimeUtils.EXISTANCE)</code>
   * </p>
   * 
   * @param entityClass
   *          The corresponding GabotoEntity class.
   * @param uri
   *          The URI of the entity.
   * 
   * @see GabotoEntity
   */
  public GabotoTimeBasedEntity(Class<? extends GabotoEntity> entityClass, String uri) {
    this(entityClass, uri, TimeUtils.EXISTANCE);
  }

  /**
   * Creates a new empty GabotoTimeBasedEntity with a defined lifespan.
   * 
   * @param entityClass
   *          The corresponding GabotoEntity class.
   * @param uri
   *          The URI of the entity.
   * @param timespan
   *          The lifetime of the object.
   * 
   * @see GabotoEntity
   */
  public GabotoTimeBasedEntity(Class<? extends GabotoEntity> entityClass, String uri, TimeSpan timespan) {
    this.entityClass = entityClass;

    // type
    this.type = GabotoOntologyLookup.getTypeURIForEntityClass(entityClass);
    if (this.type == null) {
      throw new IllegalArgumentException("Could not locate type for entityClass " + entityClass.getName());
    }

    // uri
    this.uri = uri;

    // timespan
    this.lifespan = timespan.canonicalize();
  }

  /**
   * Loads an entity from the Gaboto model.
   * 
   * 
   * @param uri
   *          The URI of the entity to be loaded.
   * @param gaboto
   *          The Gaboto model that should be used as a source.
   * 
   * @return An GabotoTimeBasedEntity that contains all the information about
   *         the entity that is stored directly in the Gaboto model.
   * 
   * @throws ResourceDoesNotExistException
   * @throws EntityDoesNotExistException
   */
  @SuppressWarnings("unchecked")
  public static GabotoTimeBasedEntity loadEntity(String uri, Gaboto gaboto) throws ResourceDoesNotExistException,
          EntityDoesNotExistException {
    logger.debug("Loading time based entity: " + uri);

    // find out the entities type
    String typeURI = gaboto.getTypeOf(uri);

    // entity class
    Class<? extends GabotoEntity> entityClass = GabotoOntologyLookup.getEntityClassFor(typeURI);

    // get entity lifetime
    TimeSpan lifetime = gaboto.getEntitysLifetime(uri);

    // create Object
    GabotoTimeBasedEntity entityTB = new GabotoTimeBasedEntity(entityClass, uri, lifetime);

    // find all graphs that are talking about the entity
    NamedGraphSet graphSet = gaboto.getNamedGraphSet();

    Iterator it = graphSet.findQuads(Node.ANY, Node.createURI(uri), Node.ANY, Node.ANY);

    Set<String> seenGraphs = new HashSet<String>();
    while (it.hasNext()) {
      Quad quad = (Quad) it.next();
      // if quad is type quad we are not interested
      if (quad.getPredicate().getURI().equals(RDF.type.getURI()))
        continue;

      String graphName = quad.getGraphName().getURI();

      if (seenGraphs.contains(graphName))
        continue;
      seenGraphs.add(graphName);

      TimeSpan ts = TimeSpan.createFromGraphName(graphName, gaboto);
      logger.debug("Found interesting timespan for tbEntity: " + ts);

      // create snapshot for graph
      GabotoSnapshot snapshot = gaboto.getSnapshot(graphName);

      // get Entity from snapshot
      GabotoEntity entity = snapshot.loadEntity(uri);

      // set timespan
      entity.setTimeSpan(ts);

      entityTB.addEntity(entity);
      logger.debug("added created entity to tbEntity. TimeSpans: " + entityTB.getTimeSpansSorted());
    }

    return entityTB;
  }

  /**
   * Returns the URI that represents this entity.
   * 
   * @return This entity's URI.
   */
  public String getUri() {
    return uri;
  }

  /**
   * Returns the life span of this entity.
   * 
   * @return The life span of the entity.
   */
  public TimeSpan getTimeSpan() {
    return lifespan;
  }

  /**
   * Returns the corresponding GabotoEntity.class.
   * 
   * @return The corresponding GabotoEntity.class.
   * 
   * @see #GabotoTimeBasedEntity(Class, String)
   * @see GabotoEntity
   */
  public Class<? extends GabotoEntity> getEntityClass() {
    return entityClass;
  }

  /**
   * Returns the corresponding ontology type URI.
   * 
   * @return The corresponding ontology type URI.
   */
  public String getType() {
    return type;
  }

  /**
   * Creates an RDF triple describing the type of this entity.
   * 
   * @return The RDF triple describing this entities type.
   */
  public Triple getRDFTypeTriple() {
    return new Triple(Node.createURI(getUri()), Node.createURI(RDF.type.getURI()), Node.createURI(getType()));
  }

  /**
   * Returns a collection of all the time spans of time based properties.
   * 
   * @return A collection of {@link TimeSpan}
   */
  private Set<TimeSpan> getTimeSpansInPropertyMap() {
    Set<TimeSpan> timespans = propertyMap.keySet();
    return timespans;
  }

  /**
   * Returns a sorted list of all timespans where this entity was stable.
   * 
   * @return A list of all timespans during which this entity was stable.
   */
  public List<TimeSpan> getTimeSpansSorted() {
    // retrieve all stored timespans
    List<TimeSpan> tmpTimespans = new ArrayList<TimeSpan>();
    tmpTimespans.addAll(getTimeSpansInPropertyMap());

    // sort them
    Collections.sort(tmpTimespans, new Comparator<TimeSpan>() {
      public int compare(TimeSpan o1, TimeSpan o2) {
        return o1.getBegin().compareTo(o2.getBegin());
      }
    });

    // add timespan to beginning and end if necessary.
    List<TimeSpan> timespans = new ArrayList<TimeSpan>();
    if (tmpTimespans.size() > 0 && !tmpTimespans.get(0).getBegin().aboutTheSame(getTimeSpan().getBegin()))
      timespans.add(TimeSpan.createFromInstants(getTimeSpan().getBegin(), tmpTimespans.get(0).getBegin()));
    else if (tmpTimespans.size() == 0)
      timespans.add(getTimeSpan());

    timespans.addAll(tmpTimespans);

    if (!timespans.get(timespans.size() - 1).getEnd().aboutTheSame(getTimeSpan().getEnd())) {
      timespans.add(TimeSpan.createFromInstants(timespans.get(timespans.size() - 1).getEnd(), getTimeSpan().getEnd()));
    }

    return timespans;
  }

  /**
   * Retrieves an entity object as it is valid at a given point in time.
   * 
   * @param ti
   *          The time instant of interest.
   * 
   * @return An GabotoEntity object for the supplied time instant.
   * @see GabotoEntity
   */
  public GabotoEntity getEntity(TimeInstant ti) {
    if (lifespan != null && !lifespan.contains(ti))
      throw new IllegalArgumentException("The supplied time instance is not in the range of this entity's lifetime.");

    GabotoEntity entity;
    try {
      entity = entityClass.newInstance();
    } catch (Exception e) {
      throw new GabotoRuntimeException(e);
    }

    entity.setUri(getUri());

    TimeInstant earliest = getTimeSpan().getBegin();
    TimeInstant latest = getTimeSpan().getEnd();

    // loop over propertyMap and try also to find the earliest possible
    // occurence
    for (Entry<TimeSpan, List<Property>> entry : propertyMap.entrySet()) {
      if (entry.getKey().contains(ti) && (!ti.aboutTheSame(entry.getKey().getEnd()) || !containsTimeSpanWithBegin(ti))) {
        // relevant property map
        for (Property p : entry.getValue()) {
          // set property in entity
          Method setter = GabotoEntityUtils.getSetMethodFor(entityClass, p.getPropertyUri());
          try {
            setter.invoke(entity, p.getValue());
          } catch (Exception e) {
            throw new GabotoRuntimeException(e);
          }
        }

        // set earliest
        if (earliest == null|| earliest.compareTo(entry.getKey().getBegin()) < 0)
          earliest = entry.getKey().getBegin();

      }
    }

    // find latest
    for (TimeSpan tsToTest : getTimeSpansSorted()) {
      if (tsToTest.getBegin().compareTo(ti) > 0) {
        latest = tsToTest.getBegin();
        break;
      }
    }

    // set properties in universalList
    for (Property p : universalProperties) {
      // set property in entity
      Method setter = GabotoEntityUtils.getSetMethodFor(entityClass, p.getPropertyUri());
      try {
        setter.invoke(entity, p.getValue());
      } catch (Exception e) {
        throw new GabotoRuntimeException(e);
      }
    }

    entity.setTimeSpan(TimeSpan.createFromInstants(earliest, latest));

    return entity;
  }

  /**
   * Returns true if any of the stored time spans starts about the same time as
   * the supplied one.
   * 
   * @param ti
   *          The time instant of interest.
   * @return Whether or not one of the stored time spans starts at this time
   *         instant.
   */
  private boolean containsTimeSpanWithBegin(TimeInstant ti) {
    for (TimeSpan tsToTest : getTimeSpansSorted())
      if (tsToTest.getBegin().aboutTheSame(ti))
        return true;

    return false;
  }

  /**
   * Adds an entity to this object.
   * 
   * @param entity
   */
  @SuppressWarnings("unchecked")
  public void addEntity(GabotoEntity entity) {
    if (!getTimeSpan().contains(entity.getTimeSpan()))
      throw new IllegalArgumentException("The entity's time span has to be contained in the object's life time.");
    try {
      this.entityClass.cast(entity);
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Supplied entity class: " + entity.getClass().getName()
              + " is not compatible with " + entityClass.getName());
    }

    // loop over methods to identify simple and complex properties
    for (Method setter : entityClass.getMethods()) {
      Method getter = null;
      String propertyURI = "";

      // is it a set method? if not continue
      if (!GabotoEntityUtils.IsSETMethod(setter))
        continue;

      // find corresponding get method
      for (Class<?> annoClassUntyped : GabotoEntityUtils.PROPERTY_ANNOTATIONS) {
        Class<? extends Annotation> annoClass = (Class<? extends Annotation>) annoClassUntyped;
        // is it annotated as a simple property
        if (setter.isAnnotationPresent(annoClass)) {
          Annotation anno = setter.getAnnotation(annoClass);

          try {
            // get annotation value
            Method valueMethod = anno.getClass().getMethod("value", (Class<?>[]) null);
            propertyURI = (String) valueMethod.invoke(anno, (Object[]) null);

            // find getter
            getter = GabotoEntityUtils.getDirectGetMethodFor(entityClass, propertyURI);
          } catch (Exception e) {
            throw new GabotoRuntimeException(e);
          }
        }
      }

      // if getter was found -> get value and add it
      if (null != getter) {
        try {
          Object value = getter.invoke(entity, (Object[]) null);
          if (null != value)
            addProperty(entity.getTimeSpan(), propertyURI, value);
        } catch (Exception e) {
          throw new GabotoRuntimeException(e);
        }
      }
    }
  }

  /**
   * Adds a time bound property to this object.
   * 
   * @param ts
   *          The time span in which the property has the specified value.
   * @param property
   *          The property.
   * @param value
   *          The value.
   */
  public void addProperty(TimeSpan ts, OntProperty property, Object value) {
    addProperty(ts, property.getURI(), value);
  }

  /**
   * Adds a time bound property to this object.
   * 
   * @param ts
   *          The time span in which the property has the specified value.
   * @param propertyURI
   *          The URI representing the property.
   * @param value
   *          The value
   */
  public void addProperty(TimeSpan ts, String propertyURI, Object value) {
    // if timespan is EXISTANCE
    if (ts.equals(TimeUtils.EXISTANCE)) {
      // put it in universal properties and get out of here.
      addProperty(propertyURI, value);
      return;
    }

    // canonicalize time span
    ts = ts.canonicalize();

    // test params
    testAddPropertyParameters(propertyURI, value);

    if (lifespan != null && !lifespan.contains(ts))
      throw new IllegalArgumentException("The supplied time span (" + ts
              + ") is not in the range of this entity's lifetime (" + lifespan + ").");

    // do we have to adjust timespans?
    if (getTimeSpan().getBegin().aboutTheSame(ts.getBegin()) && !getTimeSpan().getBegin().equals(ts.getBegin())) {
      logger.debug("Adjust begin of time span " + ts.getBegin() + " to " + getTimeSpan().getBegin());
      ts = TimeSpan.createFromInstants(getTimeSpan().getBegin(), ts.getEnd());
    }
    if (getTimeSpan().getEnd().aboutTheSame(ts.getEnd()) && !getTimeSpan().getEnd().equals(ts.getEnd())) {
      logger.debug("Adjust end of time span " + ts.getEnd() + " to " + getTimeSpan().getEnd());
      ts = TimeSpan.createFromInstants(ts.getBegin(), getTimeSpan().getEnd());
    }

    // keep a list with properties that we have to add
    List<Object[]> propertiesToAdd = new ArrayList<Object[]>();

    // erase property in universalPropertyEntity
    if (containsProperty(universalProperties, propertyURI)) {
      Property tempProp = removeProperty(universalProperties, propertyURI);

      // new small time span before the new one
      if (!ts.getBegin().aboutTheSame(getTimeSpan().getBegin())) {
        TimeSpan newBeforeTS = TimeSpan.createFromInstants(getTimeSpan().getBegin(), ts.getBegin());
        propertiesToAdd.add(new Object[] { newBeforeTS, tempProp });
      }

      // new small one after the new one
      if (!ts.getEnd().aboutTheSame(getTimeSpan().getEnd())) {
        TimeSpan newAfterTS = TimeSpan.createFromInstants(ts.getEnd(), getTimeSpan().getEnd());
        propertiesToAdd.add(new Object[] { newAfterTS, tempProp });
      }
    }

    // test if property is set in any of the already created entities
    for (Entry<TimeSpan, List<Property>> entry : propertyMap.entrySet()) {
      // test if property list contains the property
      if (containsProperty(entry.getValue(), propertyURI)) {
        // the list contains the property

        if (ts.contains(entry.getKey())) {
          // the new entry makes the old one obsolete. let's remove it
          removeProperty(entry.getValue(), propertyURI);

        } else if (entry.getKey().contains(ts)) {
          // we are contained in an old time span
          // remove property
          Property tempProp = removeProperty(entry.getValue(), propertyURI);

          // new small time span before the new one
          if (!entry.getKey().getBegin().aboutTheSame(ts.getBegin())) {
            TimeSpan newBeforeTS = TimeSpan.createFromInstants(entry.getKey().getBegin(), ts.getBegin());
            propertiesToAdd.add(new Object[] { newBeforeTS, tempProp });
          }

          // new small one after the new one
          if (!entry.getKey().getEnd().aboutTheSame(ts.getEnd())) {
            TimeSpan newAfterTS = TimeSpan.createFromInstants(ts.getEnd(), entry.getKey().getEnd());
            propertiesToAdd.add(new Object[] { newAfterTS, tempProp });
          }
        } else if (entry.getKey().overlaps(ts)) {
          // remove property
          Property tempProp = removeProperty(entry.getValue(), propertyURI);

          // 
          if (entry.getKey().getBegin().compareTo(ts.getBegin()) == -1) {
            // we overlap on the upper bound
            TimeSpan newBeforeTS = TimeSpan.createFromInstants(entry.getKey().getBegin(), ts.getBegin());
            propertiesToAdd.add(new Object[] { newBeforeTS, tempProp });
          } else {
            // we overlap on the lower bound
            TimeSpan newAfterTS = TimeSpan.createFromInstants(ts.getEnd(), entry.getKey().getEnd());
            propertiesToAdd.add(new Object[] { newAfterTS, tempProp });
          }
        }
      }
    }

    // add properties
    for (Object[] o : propertiesToAdd)
      addProperty((TimeSpan) o[0], (Property) o[1]);

    // remove empty entries from propertymap
    List<TimeSpan> emptyList = new ArrayList<TimeSpan>();
    for (Entry<TimeSpan, List<Property>> entry : propertyMap.entrySet())
      if (entry.getValue().isEmpty())
        emptyList.add(entry.getKey());
    for (TimeSpan rsToRemove : emptyList)
      propertyMap.remove(rsToRemove);

    // now we can safely create a new property
    Property newProp = new Property(propertyURI, value);
    addProperty(ts, newProp);
  }

  /**
   * Adds a property to this entity that is valid throughout its lifetime
   * 
   * <p>
   * Will overwrite any previous settings of this property
   * </p>
   * 
   * @param property
   *          The property.
   * @param value
   *          The value.
   */
  public void addProperty(OntProperty property, Object value) {
    addProperty(property.getURI(), value);
  }

  /**
   * Adds a property to this entity that is valid throughout its lifetime
   * 
   * <p>
   * Will overwrite any previous settings of this property
   * </p>
   * 
   * @param propertyURI
   *          The property represented by its URI.
   * @param value
   *          The value.
   */
  public void addProperty(String propertyURI, Object value) {
    // test params
    testAddPropertyParameters(propertyURI, value);

    // remove anything like this property from universalProperties
    removeProperty(universalProperties, propertyURI);

    // go through each entity in the map and simply erase the property
    for (Entry<TimeSpan, List<Property>> entry : propertyMap.entrySet())
      removeProperty(entry.getValue(), propertyURI);

    // add property to universalPropertyEntity
    Property newProp = new Property(propertyURI, value);
    universalProperties.add(newProp);
  }

  /**
   * Runs a couple of tests to make sure, that the corresponding entity class
   * has a method that can handle the passed parameters.
   * 
   * @param propertyURI
   *          The property represented by its id.
   * @param value
   *          The value.
   * 
   * @throws IllegalArgumentException
   *           In case some problem is detected an IllegalArgumentException is
   *           thrown.
   */
  private void testAddPropertyParameters(String propertyURI, Object value) {
    Method setter = GabotoEntityUtils.getSetMethodFor(entityClass, propertyURI);
    Method getter = GabotoEntityUtils.getDirectGetMethodFor(entityClass, propertyURI);

    if (null == setter)
      throw new IllegalArgumentException("Could not find set method for property " + propertyURI + " in class "
              + entityClass.getName());
    if (null == getter)
      throw new IllegalArgumentException("Could not find set method for property " + propertyURI + " in class "
              + entityClass.getName());

    // test getter's return value
    Class<?> returnType = getter.getReturnType();
    try {
      returnType.cast(value);
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("The get method's return type (" + returnType
              + ") and the supplied object (" + value.getClass() + ") do not work well together.", e);
    }

    // test if value is of the correct type
    Class<?>[] params = setter.getParameterTypes();
    try {
      params[0].cast(value);
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("The supplied object is not of the correct type: " + params[0].getName());
    }
  }

  /**
   * Adds a property to the internal propertyMap;
   * 
   * @param ts
   * @param property
   */
  private void addProperty(TimeSpan ts, Property property) {
    List<Property> list = null;
    if (propertyMap.containsKey(ts))
      list = propertyMap.get(ts);
    else {
      list = new ArrayList<Property>();
      propertyMap.put(ts, list);
    }

    list.add(property);
  }

  /**
   * Removes a property from the passed propertyList.
   * 
   * @param propertyList
   *          The property list that contains the property.
   * @param propertyURI
   *          The property.
   */
  private Property removeProperty(List<Property> propertyList, String propertyURI) {
    // if list contains property erase it
    Property propToDelete = null;
    for (Property p : propertyList) {
      if (p.getPropertyUri().equals(propertyURI)) {
        propToDelete = p;
        break;
      }
    }
    if (null != propToDelete)
      propertyList.remove(propToDelete);

    return propToDelete;
  }

  /**
   * Tests if property list contains the passed property.
   * 
   * @param propertyList
   *          The propert list to search through
   * @param propertyURI
   *          The property.
   * @return True if propertyList contains propertyURI.
   */
  private boolean containsProperty(List<Property> propertyList, String propertyURI) {
    for (Property p : propertyList) {
      if (p.getPropertyUri().equals(propertyURI)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns an iterator over all entities as they change over time.
   * 
   * @return An iterator over all entities as they change over time.
   */
  public Iterator<GabotoEntity> iterator() {
    return new TimeBasedEntityIterator(this);
  }

  @Override
  public String toString() {
    return "Time based entity: " + getTimeSpan() + ", " + getUri();
  }
}
