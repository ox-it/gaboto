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
package org.oucs.gaboto.entities;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.oucs.gaboto.entities.pool.EntityExistsCallback;
import org.oucs.gaboto.entities.pool.GabotoEntityPool;
import org.oucs.gaboto.entities.pool.PassiveEntitiesRequest;
import org.oucs.gaboto.entities.time.GabotoTimeBasedEntity;
import org.oucs.gaboto.entities.utils.GabotoEntityUtils;
import org.oucs.gaboto.entities.utils.SimpleLiteralProperty;
import org.oucs.gaboto.entities.utils.SimpleURIProperty;
import org.oucs.gaboto.exceptions.GabotoRuntimeException;
import org.oucs.gaboto.exceptions.IllegalAnnotationException;
import org.oucs.gaboto.model.Gaboto;
import org.oucs.gaboto.model.GabotoSnapshot;
import org.oucs.gaboto.reflection.RDFContainer;
import org.oucs.gaboto.reflection.RDFContainerTripleGeneratorImpl;
import org.oucs.gaboto.timedim.TimeSpan;
import org.oucs.gaboto.timedim.TimeUtils;
import org.oucs.gaboto.vocabulary.OxPointsVocab;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Every defined class in the Gaboto ontology needs a representation in java (a subclass of GabotoEntity).
 *
 * <h2>About GabotoEntities</h2>
 * 
 * <p>
 * OxPointEntities represent a defined class from the Gaboto ontology at a given point in time. That is, it
 * contains all the attribute values that this object had at a specified time. A representation of a Gaboto
 * object throughout its lifespan can be generated using the {@link GabotoTimeBasedEntity} class.
 * </p>
 * 
 * <p>
 * Adding new classes to the Gaboto ontology might happen frequently so a GabotoEntity is 
 * a JavaBean (see <a href="http://en.wikipedia.org/wiki/JavaBean">Wikipedia entry</a>).
 * The generation of an entity object from
 * RDF or the creation of RDF from an entity object is  handled by the Gaboto system
 * using Java's <a href="http://java.sun.com/docs/books/tutorial/reflect/index.html">reflection API</a>.
 * In order to be able to achieve this a set of annotations has been defined that have to be used
 * to mark up the various get and set methods.
 * </p>
 * 
 * <p>
 * For implementation details on how this dynamic loading of entities and their serialization to RDF
 * is done have a look at the methods {@link #loadFromSnapshot(Resource, GabotoSnapshot, GabotoEntityPool)},
 * {@link #getTriplesFor()}.
 * </p>
 * 
 * <h2>Creating new GabotoEntities</h2>
 * 
 * <p>
 * The first step when adding a new entity to the system is to adjust Gaboto' ontology and
 * add the corresponding owl:class. For example add the following:
 * <code>
 *  &lt;owl:Class rdf:ID="ComputerMonitor"/&gt;
 * </code>
 * Next we regenerate the {@link OxPointsVocab} helper class by running the ant script (build.xml target: schemas).
 * </p>
 * 
 * <p>
 * Now we can start writing the new {@link GabotoEntity}. We create a new class with
 * the same name as our ontology class and make it a subclass of {@link GabotoEntity}. We have
 * to override the getType method to return the correct ontology class. The basic skeleton would
 * look like this:
 * <pre>
 * public class ComputerMonitor extends GabotoEntity {
 *	   &#64;Override
 *     public String getType(){
 *		   return OxPointsVocab.ComputerMonitor_URI;
 *	   }
 * }
 * </pre>
 * </p>
 * 
 * <p>
 * Next we have to create a field for each bit of information that we want to store. Let's say we
 * want to store a name, the size and the monitors place. We simply add these to our class and create
 * a get and set method for each of them:
 * <code>
 * <pre>
 * public class ComputerMonitor extends GabotoEntity {
 *	   
 *     private String name;
 *     private int size;
 *     private Place place;
 * 	
 *     &#64;Override	
 *     public String getType(){
 *		   return OxPointsVocab.ComputerMonitor_URI;
 *	   }
 *     
 *     public void setName(String name){
 *         this.name = name;
 *     }
 *     public String getName(){
 *         return this.name;
 *     }
 *     
 *     public void setSize(int size){
 *         this.size = size;
 *     }
 *     public int getSize(){
 *         return this.size;
 *     }
 *     
 *     public void setPlace(Place place){
 *         this.place = place;
 *     }
 *     public Place getPlace(){
 *         return this.place;
 *     }
 * }
 * </pre>
 * </code>
 * </p>
 * 
 * <p>
 * As a last step we have to give Gaboto some clues as to what the methods should be used
 * for. This is done using predefined Annotations.
 * </p>
 *  
 * <h3>Adding annotations to guide dynamic generation and serialisation</h3>
 * 
 * <p>
 * In order to automatically create RDF from entities or load entities from RDF Gaboto relies
 * upon clues in form of predefined annotations (we call them PropertyAnnotation). All PropertyAnnotations
 * are defined in the package org.oucs.gaboto.entities.utils. For our ComputerMonitor
 * example we need the two annotations {@link SimpleLiteralProperty} and {@link SimpleURIProperty}.
 * These tell Gaboto that the marked up method returns or stores either a literal ({@link SimpleLiteralProperty})
 * or an GabotoEntity ({@link SimpleURIProperty}).
 * </p>
 * 
 * <p>
 * Additionally all PropertyAnnotations get the RDF Property the information corresponds to 
 * as a parameter.
 * 
 * <code>
 * <pre>
 * public class ComputerMonitor extends GabotoEntity {
 *	   
 *     private String name;
 *     private int size;
 *     private Place place;
 * 	
 *     &#64;Override
 *     public String getType(){
 *		   return OxPointsVocab.ComputerMonitor_URI;
 *	   }
 *     
 *     &#64;SimpleLiteralProperty(DC.title)
 *     public void setName(String name){
 *         this.name = name;
 *     }
 *     &#64;SimpleLiteralProperty(DC.title)
 *     public String getName(){
 *         return this.name;
 *     }
 *     
 *     &#64;SimpleLiteralProperty(SomeVocab.hasSize)
 *     public void setSize(int size){
 *         this.size = size;
 *     }
 *     &#64;SimpleLiteralProperty(SomeVocab.hasSize)
 *     public int getSize(){
 *         return this.size;
 *     }
 *     
 *     &#64;SimpleURIProperty(OxPointsVocab.hasParent)
 *     public void setPlace(Place place){
 *         this.place = place;
 *     }
 *     &#64;SimpleURIProperty(OxPointsVocab.hasParent)
 *     public Place getPlace(){
 *         return this.place;
 *     }
 * }
 * </pre>
 * </code>
 * </p>
 * 
 * <h3>Adding annotations to guide the transformation to various output formats</h3>
 * 
 * <p>
 * The transformation of {@link GabotoEntity}s is also guided through annotations (TransformationAnnotation).
 * There are two TransformationAnnotaions: {@link TransformationProperty} and {@link TransformationIndirectProperty}.
 * The {@link TransformationProperty} annotation tells the transformer that the annotated method returns the object
 * it is looking for for the supplied RDF Property. The {@link TransformationIndirectProperty} annotation on the other
 * hand tells the transformer that the method's return value is an GabotoEntity and that it should look for the
 * object it is looking for (defined by the RDF property) there.
 * </p>
 * 
 * <p>
 * In this case we want to mark up the getName method with {@link TransformationProperty} and for the monitor's location
 * we tell the transformer to look at the Place object using the {@link TransformationIndirectProperty} annotation.
 * 
 * <code>
 * <pre>
 * public class ComputerMonitor extends GabotoEntity {
 *	   
 *     private String name;
 *     private int size;
 *     private Place place;
 * 	
 *     &#64;Override
 *     public String getType(){
 *		   return OxPointsVocab.ComputerMonitor_URI;
 *	   }
 *     
 *     
 *     &#64;SimpleLiteralProperty(DC.title)
 *     public void setName(String name){
 *         this.name = name;
 *     }
 *     &#64;TransformationProperty(DC.title)
 *     &#64;SimpleLiteralProperty(DC.title)
 *     public String getName(){
 *         return this.name;
 *     }
 *     
 *     &#64;SimpleLiteralProperty(SomeVocab.hasSize)
 *     public void setSize(int size){
 *         this.size = size;
 *     }
 *     &#64;SimpleLiteralProperty(SomeVocab.hasSize)
 *     public int getSize(){
 *         return this.size;
 *     }
 *     
 *     &#64;SimpleURIProperty(OxPointsVocab.hasParent)
 *     public void setPlace(Place place){
 *         this.place = place;
 *     }
 *     &#64;TransformationIndirectProperty(OxPointsVocab.hasLocation)
 *     &#64;SimpleURIProperty(OxPointsVocab.hasParent)
 *     public Place getPlace(){
 *         return this.place;
 *     }
 * }
 * </pre>
 * </code>
 * </p>
 * 
 * 
 * @author Arno Mittelbach
 * @version 0.1
 * 
 * @see GabotoTimeBasedEntity
 * @see GabotoEntityPool
 */
abstract public class GabotoEntity implements RDFContainer {

	private static Logger logger = Logger.getLogger(GabotoEntity.class.getName());
	
	/**
	 * Stores the timespan in which this entity is valid. If it is null, then the entity is valid indefinitely. 
	 */
	private TimeSpan timespan = TimeUtils.EXISTENCE;
	
	/**
	 * stores the entity's URI.
	 */
	private String uri;
	
	/**
	 * Stores the pool, this entity was created from
	 */
	private GabotoEntityPool createFromPool = null;

	
	/**
	 * Tells whether or not passive entities were loaded
	 */
	private boolean passiveEntitiesLoaded = false;
	
	/**
	 * 
	 */
	protected Map<String, Resource> missingEntityReferences = new HashMap<String, Resource>();
	protected Map<String, Collection<EntityExistsCallback>> missingEntityReferenceCallbacks = new HashMap<String, Collection<EntityExistsCallback>>();
	
	/**
	 * Instantiate the GabotoEntity. 
	 * 
	 * <p>
	 * OxPointsEntities should always use the default constructor and provide
	 * methods for all their fields in order for Gaboto to be able to automatically
	 * load and serialize them.
	 * </p>
	 */
	public GabotoEntity(){
		
	}
	
	final static public <T extends GabotoEntity> T createNew(Gaboto gaboto, T newEntity){
		newEntity.setUri(gaboto.generateID());
		return newEntity;
	}
	
	final public void setCreatedFromPool(GabotoEntityPool pool){
		this.createFromPool = pool;
	}
	
	final protected void addMissingReference(Resource res, EntityExistsCallback callback){
		missingEntityReferences.put(res.getURI(), res);
		if(! missingEntityReferenceCallbacks.containsKey(res.getURI())){
			missingEntityReferenceCallbacks.put(res.getURI(), new HashSet<EntityExistsCallback>());
		}
		missingEntityReferenceCallbacks.get(res.getURI()).add(callback);
	}
	
	final protected void removeMissingReference(String uriToRemove){
		missingEntityReferences.remove(uriToRemove);
		//missingEntityReferenceCallbacks.remove(uri);
	}
	
	
	/**
	 * Returns the entity's URI.
	 * 
	 * @see #setUri(String)
	 * @return the entity's URI.
	 */
	final public String getUri() {
		return uri;
	}
	
	/**
	 * Sets the entity's URI.
	 * 
	 * @see #getUri()
	 * @param uri this entity's new URI.
	 */
	final public void setUri(String uri) {
		this.uri = uri;
	}
	
	/**
	 * Stores the lifespan of this GabotoEntity.
	 * 
	 * <p>
	 * The lifespan does not necessary correspond to when the entity came into
	 * being and was destroyed. It is the span in which the entity is valid in the
	 * form at it is represented by this object.
	 * </p>
	 * 
	 * @see #getTimeSpan()
	 * 
	 * @param ts The lifespan
	 */
	final public void setTimeSpan(TimeSpan ts){
		if(ts == null)
			this.timespan = TimeUtils.EXISTENCE;
		else
			this.timespan = ts.canonicalize();
	}
	
	/**
	 * Returns the entity's lifespan.
	 * 
	 * @see #setTimeSpan(TimeSpan)
	 * @return Returns the entity's lifespan.
	 */
	final public TimeSpan getTimeSpan(){
		return this.timespan;
	}
	
	/**
	 * Every entity has a defined type. 
	 * 
	 * @see OxPointsVocab
	 * 
	 * @return The entity's type (ontology class). 
	 */
	abstract public String getType();

	/**
   * @param propertyURI unused, overridden 
   */
	protected List<Method> getIndirectMethodsForProperty(String propertyURI){
		return null;
	}
	
	/**
	 * Tells whether direct references have been resolved for this entity.
	 * 
	 * @return true if direct references have been resolved.
	 */
	public boolean isDirectReferencesResolved() {
		return missingEntityReferences.isEmpty();
	}

	/**
	 * Tries to resolve the direct references from the entity's pool (the pool it was last added to).
	 */
	public void resolveDirectReferences(){
		resolveDirectReferences(createFromPool);
	}

	/**
	 * Tries to resolve the entity's direct references from the passed pool.
	 * @param pool The pool to load the direct references from.
	 */
	public void resolveDirectReferences(GabotoEntityPool pool) {
		if(isDirectReferencesResolved())
			return;
	
		if(pool == null)
			throw new IllegalStateException("The GabotoEntity was not provided with a pool object to resolve its references from.");
		
		// add missing references
		pool.addMissingReferencesForEntity(missingEntityReferences.values(), missingEntityReferenceCallbacks);
	}

	/**
	 * Tells the entity that all passive properties were loaded.
	 */
	public void setPassiveEntitiesLoaded(){
		this.passiveEntitiesLoaded = true;
	}
	
	/**
	 * 
	 * @return true if passive entities were loaded.
	 */
	public boolean isPassiveEntitiesLoaded(){
		return passiveEntitiesLoaded;
	}
	
	/**
	 * Tries to load passive entities from the entity's pool.
	 */
	public void loadPassiveEntities(){
		loadPassiveEntities(createFromPool);
	}
		
	/**
	 * Tries to load passive entities from the passed pool.
	 * @param pool The pool to load the passive entities from.
	 */
	public void loadPassiveEntities(GabotoEntityPool pool){
		if(passiveEntitiesLoaded)
			return;
		
		if(pool == null)
			throw new IllegalStateException("The GabotoEntity was not provided with a pool object to load the passive properties from.");

		pool.addPassiveEntitiesFor(this);
	}

	/**
	 * Is overridden by subclasses to ask for passive entities that they claim belong to them.
	 *  
	 * @return null
	 */
	public Collection<PassiveEntitiesRequest> getPassiveEntitiesRequest(){
		return null;
	}
	
	/**
	 * The entity adds itself to the supplied Jena Model.
	 * 
	 * @param model the JenaModel
	 * @throws IllegalAnnotationException 
	 */
	public void addToModel(Model model) throws IllegalAnnotationException{
		Graph g = model.getGraph();
		List<Triple> triples = getTriplesFor();
		for(Triple t : triples)
			g.add(t);
	}
	
	/**
	 * Returns the value of a property via reflection (searching in direct and indirect properties, but not in passive properties)
	 * 
	 * @param prop The property
	 * @return The property's value (or null).
	 */
	public Object getPropertyValue(Property prop){
		return getPropertyValue(prop.getURI(), false, true);
	}
	
	/**
	 * Returns the value of a property via reflection
	 * 
	 * @param prop The property
	 * @param searchInPassiveProperties Search in passive properties
	 * @param searchInIndirectProperties Search in indirect properties
	 */
	public Object getPropertyValue(Property prop, boolean searchInPassiveProperties, boolean searchInIndirectProperties){
		return getPropertyValue(prop.getURI(), searchInPassiveProperties, searchInIndirectProperties);
	}
	
	/**
	 * Returns the value of a property via reflection (searching in direct and indirect properties, but not in passive properties)
	 * 
	 * @param propURI The property's URI
	 */
	public Object getPropertyValue(String propURI){
		return getPropertyValue(propURI, false, true);
	}
	
	/**
	 * Returns the value of a property.
	 * 
	 * @param propURI The property's URI.
	 * @param searchInPassiveProperties True to search in passive properties.
	 * @param searchInIndirectProperties True to search in indirect properties.
	 */
	@SuppressWarnings("unchecked")
  public Object getPropertyValue(String propURI, boolean searchInPassiveProperties, boolean searchInIndirectProperties){
		Method directMethod = GabotoEntityUtils.getGetMethodFor(this, propURI);
		
		if(null != directMethod){
			try {
				Object value = directMethod.invoke(this, (Object[])null);
				if(value != null)
					return value;
			} catch (Exception e) {
				throw new GabotoRuntimeException(e);
			}
		}
		
		// search in passive
		if(searchInPassiveProperties){
			Object value = getPassivePropertyValue(propURI);
			if(value != null)
				return value;
		}

		// search in indirect properties?
		if(! searchInIndirectProperties)
			return null;
		
		// look for indirect Method
		List<Method> indirectMethods = getIndirectMethodsForProperty(propURI);
		if(indirectMethods == null)
			return null;
		
		for(Method indirectMethod : indirectMethods){
			try {
				Object obj = indirectMethod.invoke(this, (Object[])null);
				if(obj != null){
					if(! (obj instanceof GabotoEntity) && !(obj instanceof Collection))
						throw new IllegalAnnotationException(getClass());
					
					if(obj instanceof GabotoEntity){
						// cast
						GabotoEntity entity = (GabotoEntity) obj;
						
						// try to find answer at entity
						Object value = entity.getPropertyValue(propURI, searchInPassiveProperties, searchInIndirectProperties);
						if(value != null)
							return value;
					} else if(obj instanceof Collection){
						// try to cast
						try{
							Collection<GabotoEntity> entityCollection = (Collection<GabotoEntity>) obj;
							for(GabotoEntity entityInCollection : entityCollection){
								// try to find answer at entity
								Object value = entityInCollection.getPropertyValue(propURI, searchInPassiveProperties, searchInIndirectProperties);
								if(null != value)
									return value;
							}
						} catch(ClassCastException e){
							IllegalAnnotationException iae = new IllegalAnnotationException(getClass());
							iae.initCause(e);
							throw iae;	
						}
					}
					
				}
      } catch (Exception e) {
        throw new GabotoRuntimeException(e);
      } 
		}
		
		return null;
	}
	
	/**
	 * Returns the value of a passive property.
	 * @param prop The passive property.
	 * @return The property's value
	 */
	public Object getPassivePropertyValue(Property prop){
		return getPassivePropertyValue(prop.getURI());
	}
	
	/**
	 * Returns the value of a passive property.
	 * @param propURI The URI of the passive property.
	 * @return The property's value
	 */
	public Object getPassivePropertyValue(String propURI){
		Method m = GabotoEntityUtils.getPassiveGetMethodFor(this.getClass(), propURI);
		if(m != null){
			try {
	      System.err.println("For class " + this.getClass() + 
	              " found passive method " + m.getName() + ":" + m.invoke(this, (Object[])null));
				return m.invoke(this, (Object[])null);
      } catch (Exception e) {
        throw new GabotoRuntimeException(e);
      } 
		}
		
		return null;
	}	
	
	/**
	 * Creates a map with all properties: direct (including static and unstored), indirect and passive
	 */
	public Map<String, Object> getAllProperties(){
		Map<String, Object> properties = getAllDirectProperties();
		properties.putAll(getAllPassiveProperties());
		properties.putAll(getAllIndirectProperties());
		
		return properties;
	}
	
	/**
	 * Creates a map with property value pairs for all direct properties (including static and unstored).
	 * @return a map with property value pairs 
	 */
	public Map<String, Object> getAllDirectProperties(){
		Map<String, Object> properties = new HashMap<String, Object>();
		
		for(String prop : GabotoEntityUtils.getAllDirectProperties(this.getClass())){
			properties.put(prop, getPropertyValue(prop));
		}
		
		return properties;
	}
	
	/**
	 * Creates a map with property value pairs for all passive properties.
	 * @return a map with property value pairs.
	 */
	public Map<String, Object> getAllPassiveProperties(){
		Map<String, Object> properties = new HashMap<String, Object>();
		
		for(String prop : GabotoEntityUtils.getAllPassiveProperties(this.getClass())){
			properties.put(prop, getPassivePropertyValue(prop));
		}
		
		return properties;
	}
	
	/**
	 * Creates a map with property value pairs for all indirect properties.
	 * @return a map with property value pairs.
	 */
	public Map<String, Object> getAllIndirectProperties(){
		Map<String, Object> properties = new HashMap<String, Object>();
		
		for(String prop : GabotoEntityUtils.getAllIndirectProperties(this.getClass())){
			properties.put(prop, getPropertyValue(prop, false, true));
		}
		
		return properties;
	}
	
	/**
	 * Creates the java representation from an GabotoSnapshot and a resource object..
	 * 
	 * <p>
	 * The method uses reflection mechanisms to find appropriate setter methods.
	 * </p>
	 * 
	 * @param res An RDF Resource.
	 * @param snapshot The GabotoSnapshot containing all the data.
	 * @param pool The entity pool that is currently created (used for references to other entities).
	 *
	 */
	public void loadFromSnapshot(Resource res, GabotoSnapshot snapshot, GabotoEntityPool pool) {
		// set uri
		this.setUri(res.getURI());

		// log
		logger.debug("Load entity " + this.getUri() + " from Snapshot.");
		
		// try to set time span
		this.setTimeSpan(snapshot.getTimeSpanForEntity(res));
		
		// load entity
		//RDFContainerLoaderImpl.getInstance().loadFromSnapshot(this, res, snapshot, pool);
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
	public List<Triple> getTriplesFor() throws IllegalAnnotationException{
		return getTriplesFor(true);
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
	public List<Triple> getTriplesFor(boolean includeType) throws IllegalAnnotationException{
		// if no uri
		if(this.getUri() == null)
			throw new IllegalArgumentException("Entities need to have a defined uri");
		
		List<Triple> triples = RDFContainerTripleGeneratorImpl.getInstance().getTriplesFor(this, Node.createURI(getUri()), includeType);
		
		return triples;
	}
	
	
	@Override
	public String toString(){
		TimeSpan ts = getTimeSpan();
		if(ts == null)
			ts = TimeUtils.EXISTENCE;
		
		return getUri() + " " + this.getClass().getSimpleName() + " : " + ts + "";
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if(! (obj instanceof GabotoEntity))
			return false;
		
		GabotoEntity entity = (GabotoEntity) obj;
		
		if(null != getTimeSpan() && null != getUri() && null != entity.getTimeSpan() && null != entity.getUri())
			return getTimeSpan().equals(entity.getTimeSpan()) && getUri().equals(entity.getUri()) && getClass().equals(entity.getClass());
		
		if(null != getUri() && null != entity.getUri())
			return getUri().equals(entity.getUri()) && getClass().equals(entity.getClass());
		
		return super.equals(obj);
	}
	
	
	@Override
	public int hashCode(){
		return toString().hashCode();
	}
}
