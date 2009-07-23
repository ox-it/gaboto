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
package org.oucs.gaboto.entities.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.oucs.gaboto.GabotoRuntimeException;
import org.oucs.gaboto.beans.GabotoBean;
import org.oucs.gaboto.entities.GabotoEntity;
import org.oucs.gaboto.entities.IllegalAnnotationException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.AnonId;

/**
 * Utility class with various helper methods to work with {@link GabotoEntity}s.
 * 
 * @author Arno Mittelbach
 * @version 0.1
 * 
 * @see GabotoEntity
 */
public class GabotoEntityUtils {

  /**
   * Used for a simple caching mechanism.
   */
  private static Map<String, Method> getMethodCache = new HashMap<String, Method>();
  private static Map<String, Method> setMethodCache = new HashMap<String, Method>();

  private static Collection<String> getMethodInCache = new HashSet<String>();

  private static Map<String, Method> getBeanMethodCache = new HashMap<String, Method>();

  private static Collection<String> getBeanMethodInCache = new HashSet<String>();

  private static Map<String, Collection<String>> getAllPropertiesCache = new HashMap<String, Collection<String>>();
  private static Map<String, Collection<String>> getAllPassivePropertiesCache = new HashMap<String, Collection<String>>();
  private static Map<String, Collection<String>> getAllIndirectPropertiesCache = new HashMap<String, Collection<String>>();
  private static Map<String, Collection<String>> getAllBeanPropertiesCache = new HashMap<String, Collection<String>>();

  /**
   * An array containing all PropertyAnnotations.
   */
  public static final Class<?>[] PROPERTY_ANNOTATIONS = new Class<?>[] {
      SimpleURIProperty.class, SimpleLiteralProperty.class,
      ComplexProperty.class, BagURIProperty.class, BagLiteralProperty.class,
      BagComplexProperty.class };

  public static final Class<UnstoredProperty> UNSTORED_PROPERTY_ANNOTATION = UnstoredProperty.class;

  public static final Class<StaticProperty> STATIC_PROPERTY_ANNOTATION = StaticProperty.class;

  public static final Class<PassiveProperty> PASSIVE_PROPERTY_ANNOTATION = PassiveProperty.class;

  public static final Class<IndirectProperty> INDIRECT_PROPERTY_ANNOTATION = IndirectProperty.class;

  /**
   * Creates an anonymous node (blank node) for the use in {@link GabotoBean}s.
   * 
   * @param uri
   *          The URI of the corresponding entity.
   * @param propertyUri
   *          The URI of the corresponding property.
   * 
   * @return A new blank node with a reconstructible URI.
   * @see GabotoBean
   */
  public static Node createAnonForBean(String uri, String propertyUri) {
    return Node.createAnon(new AnonId(uri + "#bean-" + propertyUri.hashCode()));
  }

  /**
   * Creates an anonymous node (blank node) for the use with RDF bags in
   * {@link GabotoEntity}s.
   * 
   * @param uri
   *          The URI of the corresponding entity.
   * @param propertyUri
   *          The URI of the corresponding property.
   * 
   * @return A new blank node with a reconstructible URI.
   * 
   * @see GabotoEntity#getTriplesFor()
   */
  public static Node createAnonForBag(String uri, String propertyUri) {
    return Node.createAnon(new AnonId(uri + "#bag-" + propertyUri.hashCode()));
  }

  /**
   * Maps an Gaboto ontology class to its java representative.
   * 
   * @param type
   *          The Gaboto ontology class
   * @return The {@link GabotoEntity} that represents the ontology class in
   *         java.
   * 
   * @see GabotoEntity
   */
  @SuppressWarnings("unchecked")
  public static Class<? extends GabotoEntity> getEntityClassFor(OntClass type) {
    String packageName = GabotoEntity.class.getPackage().getName();
    String localName = type.getLocalName();

    try {
      Class clazz = Class.forName(packageName + "." + localName);
      return clazz;
    } catch (ClassNotFoundException e) {
      IllegalArgumentException iae = new IllegalArgumentException(
          "No class found for " + type.getURI());
      iae.initCause(e);
      throw iae;
    }
  }

  @SuppressWarnings("unchecked")
  public static Collection<String> getAllBeanProperties(
      Class<? extends GabotoBean> beanClass) {
    if (getAllBeanPropertiesCache.containsKey(beanClass.getName()))
      return getAllBeanPropertiesCache.get(beanClass.getName());

    Collection<String> properties = new HashSet<String>();
    for (Method method : beanClass.getMethods()) {
      for (Class<?> annoClass : PROPERTY_ANNOTATIONS) {
        if (method.isAnnotationPresent((Class<? extends Annotation>) annoClass)) {
          Annotation anno = method
              .getAnnotation((Class<? extends Annotation>) annoClass);
          Method valueMethod;
          try {
            valueMethod = anno.getClass().getMethod("value", (Class<?>[]) null);
            Object value = valueMethod.invoke(anno, (Object[]) null);

            if (!(value instanceof String))
              throw new IllegalAnnotationException(annoClass);

            if (IsGETMethod(method))
              properties.add((String) value);
          } catch (Exception e) {
            throw new GabotoRuntimeException(e);
          }
          break;
        }
      }
    }

    // store in cache
    getAllBeanPropertiesCache.put(beanClass.getName(), properties);

    return properties;
  }

  @SuppressWarnings("unchecked")
  public static Collection<String> getAllDirectProperties(
      Class<? extends GabotoEntity> entityClass) {
    if (getAllPropertiesCache.containsKey(entityClass.getName()))
      return getAllPropertiesCache.get(entityClass.getName());

    // annotations to search In
    Collection<Class<?>> annotationsToSearchIn = new HashSet<Class<?>>();
    for (Class<?> annoClass : PROPERTY_ANNOTATIONS)
      annotationsToSearchIn.add(annoClass);
    annotationsToSearchIn.add(STATIC_PROPERTY_ANNOTATION);
    annotationsToSearchIn.add(UNSTORED_PROPERTY_ANNOTATION);

    Collection<String> properties = new HashSet<String>();
    for (Method method : entityClass.getMethods()) {
      for (Class<?> annoClass : annotationsToSearchIn) {
        if (method.isAnnotationPresent((Class<? extends Annotation>) annoClass)) {
          Annotation anno = method
              .getAnnotation((Class<? extends Annotation>) annoClass);
          Method valueMethod;
          try {
            if (anno instanceof UnstoredProperty) {
              String[] values = ((UnstoredProperty) anno).value();

              if (!IsGETMethod(method))
                throw new IllegalAnnotationException(entityClass);

              for (String value : values)
                properties.add(value);
            } else {
              valueMethod = anno.getClass().getMethod("value",
                  (Class<?>[]) null);
              Object value = valueMethod.invoke(anno, (Object[]) null);

              if (!(value instanceof String))
                throw new IllegalAnnotationException(entityClass);

              if (IsGETMethod(method))
                properties.add((String) value);
            }
          } catch (Exception e) {
            throw new GabotoRuntimeException(e);
          }
        }
      }
    }

    // store in cache
    getAllPropertiesCache.put(entityClass.getName(), properties);

    return properties;
  }

  public static Collection<String> getAllPassiveProperties(
      Class<? extends GabotoEntity> entityClass) {
    if (getAllPassivePropertiesCache.containsKey(entityClass.getName()))
      return getAllPassivePropertiesCache.get(entityClass.getName());

    Collection<String> properties = new HashSet<String>();
    for (Method method : entityClass.getMethods()) {

      if (method.isAnnotationPresent(PASSIVE_PROPERTY_ANNOTATION)) {
        PassiveProperty anno = method
            .getAnnotation(PASSIVE_PROPERTY_ANNOTATION);
        String value = anno.uri();

        if (IsGETMethod(method))
          properties.add(value);
      }
    }

    // store in cache
    getAllPassivePropertiesCache.put(entityClass.getName(), properties);

    return properties;
  }

  public static Collection<String> getAllIndirectProperties(
      Class<? extends GabotoEntity> entityClass) {
    if (getAllIndirectPropertiesCache.containsKey(entityClass.getName()))
      return getAllIndirectPropertiesCache.get(entityClass.getName());

    Collection<String> properties = new HashSet<String>();
    for (Method method : entityClass.getMethods()) {

      if (method.isAnnotationPresent(INDIRECT_PROPERTY_ANNOTATION)) {
        IndirectProperty anno = method.getAnnotation(INDIRECT_PROPERTY_ANNOTATION);
        String[] values = anno.value();

        if (IsGETMethod(method))
          for (String value : values)
            properties.add(value);
      }
    }

    // store in cache
    getAllIndirectPropertiesCache.put(entityClass.getName(), properties);

    return properties;
  }

  /**
   * Tries to find a set Method for a given property in the passed
   * {@link GabotoEntity}.
   * 
   * @param entityClass
   *          The entity class to look for the method.
   * @param propertyURI
   *          The property.
   * 
   * @return null or the set Method
   * 
   * @see GabotoEntity
   * @see #getDirectGetMethodFor(Class, String)
   */
  @SuppressWarnings("unchecked")
  public static Method getSetMethodFor(
      Class<? extends GabotoEntity> entityClass, String propertyURI) {
    String key = entityClass.getName() + "#" + propertyURI;
    if (setMethodCache.containsKey(key))
      return setMethodCache.get(key);

    for (Method setter : entityClass.getMethods()) {
      // is it annotated as a simple property
      for (Class<?> annoClass : PROPERTY_ANNOTATIONS) {
        if (setter.isAnnotationPresent((Class<? extends Annotation>) annoClass)) {
          Annotation anno = setter
              .getAnnotation((Class<? extends Annotation>) annoClass);
          try {
            Method valueMethod = anno.getClass().getMethod("value",
                (Class<?>[]) null);
            Object value = valueMethod.invoke(anno, (Object[]) null);

            if (!(value instanceof String))
              continue;

            if (!((String) value).equals(propertyURI))
              continue;

            // is it a set method
            if (!GabotoEntityUtils.IsSETMethod(setter))
              continue;

            setMethodCache.put(key, setter);
            return setter;
          } catch (Exception e) {
            throw new GabotoRuntimeException(e);
          }
        }
      }
    }

    return null;
  }

  /**
   * Finds direct, static and unstored get methods.
   * 
   * @param entity
   * @param propertyURI
   * @return Object{Method, GabotoEntity} or null.
   */
  public static Method getGetMethodFor(GabotoEntity entity, String propertyURI) {
    // test for a direct method
    Method m = getDirectGetMethodFor(entity.getClass(), propertyURI);
    if (null != m)
      return m;
    // test for a static method
    m = getStaticGetMethodFor(entity.getClass(), propertyURI);
    if (null != m)
      return m;
    // test for a unstored method
    m = getUnstoredGetMethodFor(entity.getClass(), propertyURI);
    if (null != m)
      return m;

    return null;
  }

  public static Method getPassiveGetMethodFor(
      Class<? extends GabotoEntity> entityClass, String propertyURI) {
    String key = entityClass.getName() + "#passive-" + propertyURI;
    if (getMethodCache.containsKey(key))
      return getMethodCache.get(key);

    // did we already look for it and not find it?
    if (getMethodInCache.contains(key))
      return null;
    getMethodInCache.add(key);

    // search for it
    for (Method getter : entityClass.getMethods()) {
      // is it annotated as a simple property
      if (getter.isAnnotationPresent(PASSIVE_PROPERTY_ANNOTATION)) {
        PassiveProperty anno = getter
            .getAnnotation(PASSIVE_PROPERTY_ANNOTATION);
        String value = anno.uri();

        if (!propertyURI.equals(value))
          continue;

        // is it a get method
        if (!GabotoEntityUtils.IsGETMethod(getter))
          continue;

        // put into cache
        getMethodCache.put(key, getter);

        return getter;
      }
    }

    return null;
  }

  public static Method getStaticGetMethodFor(
      Class<? extends GabotoEntity> entityClass, String propertyURI) {
    String key = entityClass.getName() + "#static-" + propertyURI;
    if (getMethodCache.containsKey(key))
      return getMethodCache.get(key);

    // did we already look for it?
    if (getMethodInCache.contains(key))
      return null;
    getMethodInCache.add(key);

    // search for it
    for (Method getter : entityClass.getMethods()) {
      // is it annotated as a simple property
      if (getter.isAnnotationPresent(STATIC_PROPERTY_ANNOTATION)) {
        StaticProperty anno = getter.getAnnotation(STATIC_PROPERTY_ANNOTATION);
        String value = anno.value();

        if (!propertyURI.equals(value))
          continue;

        // is it a get method
        if (!GabotoEntityUtils.IsGETMethod(getter))
          continue;

        // put into cache
        getMethodCache.put(key, getter);

        return getter;
      }
    }

    return null;
  }

  public static Method getUnstoredGetMethodFor(
      Class<? extends GabotoEntity> entityClass, String propertyURI) {
    String key = entityClass.getName() + "#unstored-" + propertyURI;
    if (getMethodCache.containsKey(key))
      return getMethodCache.get(key);

    // did we already look for it?
    if (getMethodInCache.contains(key))
      return null;
    getMethodInCache.add(key);

    // search for it
    for (Method getter : entityClass.getMethods()) {
      // is it annotated as a simple property
      if (getter.isAnnotationPresent(UNSTORED_PROPERTY_ANNOTATION)) {
        UnstoredProperty anno = getter
            .getAnnotation(UNSTORED_PROPERTY_ANNOTATION);
        String[] values = anno.value();

        if (!ArrayUtils.contains(values, propertyURI))
          continue;

        // is it a get method
        if (!GabotoEntityUtils.IsGETMethod(getter))
          continue;

        // put into cache
        getMethodCache.put(key, getter);

        return getter;
      }
    }

    return null;
  }

  /**
   * Tries to find a get Method for a given property in the passed
   * {@link GabotoEntity}.
   * 
   * @param entityClass
   *          The entity class to look for the method.
   * @param propertyURI
   *          The property.
   * 
   * @return null or the get Method
   * 
   * @see GabotoEntity
   * @see #getSetMethodFor(Class, String)
   */
  @SuppressWarnings("unchecked")
  public static Method getDirectGetMethodFor(
      Class<? extends GabotoEntity> entityClass, String propertyURI) {
    String key = entityClass.getName() + "#" + propertyURI;
    if (getMethodCache.containsKey(key))
      return getMethodCache.get(key);

    // did we already look for it?
    if (getMethodInCache.contains(key))
      return null;
    getMethodInCache.add(key);

    // search for it
    for (Method getter : entityClass.getMethods()) {
      // is it annotated as a simple property
      for (Class<?> annoClass : PROPERTY_ANNOTATIONS) {
        if (getter.isAnnotationPresent((Class<? extends Annotation>) annoClass)) {
          Annotation anno = getter
              .getAnnotation((Class<? extends Annotation>) annoClass);
          try {
            Method valueMethod = anno.getClass().getMethod("value",
                (Class<?>[]) null);
            Object value = valueMethod.invoke(anno, (Object[]) null);

            if (!(value instanceof String))
              continue;

            if (!((String) value).equals(propertyURI))
              continue;

            // is it a get method
            if (!GabotoEntityUtils.IsGETMethod(getter))
              continue;

            getMethodCache.put(key, getter);

            return getter;
          } catch (Exception e) {
            throw new GabotoRuntimeException(e);
          }
        }
      }
    }

    return null;
  }

  @SuppressWarnings("unchecked")
  public static Method getBeanGetMethodFor(
      Class<? extends GabotoBean> beanClass, String propertyURI) {
    String key = beanClass.getName() + "#" + propertyURI;
    if (getBeanMethodCache.containsKey(key))
      return getBeanMethodCache.get(key);

    // did we already look for it?
    if (getBeanMethodInCache.contains(key))
      return null;
    getBeanMethodInCache.add(key);

    // search for it
    for (Method getter : beanClass.getMethods()) {
      // is it annotated as a simple property
      for (Class<?> annoClass : PROPERTY_ANNOTATIONS) {
        if (getter.isAnnotationPresent((Class<? extends Annotation>) annoClass)) {
          Annotation anno = getter
              .getAnnotation((Class<? extends Annotation>) annoClass);
          try {
            Method valueMethod = anno.getClass().getMethod("value",
                (Class<?>[]) null);
            Object value = valueMethod.invoke(anno, (Object[]) null);

            if (!(value instanceof String))
              continue;

            if (!((String) value).equals(propertyURI))
              continue;

            // is it a get method
            if (!GabotoEntityUtils.IsGETMethod(getter))
              continue;

            getBeanMethodCache.put(key, getter);

            return getter;
          } catch (Exception e) {
            throw new GabotoRuntimeException(e);
          }
        }
      }
    }

    return null;
  }

  /**
   * Tests if the supplied method is a get method.
   * 
   * @param method
   *          The method to test.
   * @return True if it is a get method.
   */
  @SuppressWarnings("unchecked")
  public static boolean IsGETMethod(Method method) {
    boolean found = false;
    for (Class<?> annoClass : PROPERTY_ANNOTATIONS) {
      if (method.isAnnotationPresent((Class<? extends Annotation>) annoClass)) {
        found = true;
        break;
      }
    }
    if (!found && method.isAnnotationPresent(PASSIVE_PROPERTY_ANNOTATION))
      found = true;
    if (!found && method.isAnnotationPresent(INDIRECT_PROPERTY_ANNOTATION))
      found = true;
    if (!found && method.isAnnotationPresent(STATIC_PROPERTY_ANNOTATION))
      found = true;

    if (!found)
      return false;

    Class<?>[] params = method.getParameterTypes();
    Class<?> c = method.getReturnType();

    return !c.getName().equals("void") && params.length == 0;
  }

  /**
   * Tests if the supplied method is a set method.
   * 
   * @param method
   *          The method to test.
   * @return True if it is a set method.
   */
  @SuppressWarnings("unchecked")
  public static boolean IsSETMethod(Method method) {
    boolean found = false;
    for (Class<?> annoClass : PROPERTY_ANNOTATIONS) {
      if (method.isAnnotationPresent((Class<? extends Annotation>) annoClass)) {
        found = true;
        break;
      }

    }
    if (!found)
      return false;

    Class<?>[] params = method.getParameterTypes();
    Class<?> c = method.getReturnType();

    return c.getName().equals("void") && params.length == 1;
  }
}
