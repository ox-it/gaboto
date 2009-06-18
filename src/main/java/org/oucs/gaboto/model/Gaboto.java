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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.oucs.gaboto.GabotoConfiguration;
import org.oucs.gaboto.GabotoLibrary;
import org.oucs.gaboto.entities.GabotoEntity;
import org.oucs.gaboto.entities.time.GabotoTimeBasedEntity;
import org.oucs.gaboto.exceptions.CorruptDataException;
import org.oucs.gaboto.exceptions.CorruptTimeIndexException;
import org.oucs.gaboto.exceptions.EntityAlreadyExistsException;
import org.oucs.gaboto.exceptions.EntityDoesNotExistException;
import org.oucs.gaboto.exceptions.GabotoException;
import org.oucs.gaboto.exceptions.IllegalAnnotationException;
import org.oucs.gaboto.exceptions.NoTimeIndexSetException;
import org.oucs.gaboto.exceptions.ResourceDoesNotExistException;
import org.oucs.gaboto.model.events.GabotoEvent;
import org.oucs.gaboto.model.events.GabotoInsertionEvent;
import org.oucs.gaboto.model.events.GabotoRemovalEvent;
import org.oucs.gaboto.model.listener.UpdateListener;
import org.oucs.gaboto.timedim.TimeInstant;
import org.oucs.gaboto.timedim.TimeSpan;
import org.oucs.gaboto.timedim.TimeUtils;
import org.oucs.gaboto.timedim.index.TimeDimensionIndexer;
import org.oucs.gaboto.vocabulary.RDFCON;
import org.oucs.gaboto.vocabulary.RDFG;
import org.oucs.gaboto.vocabulary.TimeVocab;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import de.fuberlin.wiwiss.ng4j.NamedGraph;
import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
import de.fuberlin.wiwiss.ng4j.Quad;

/**
 * Forms the gateway to the low level data model.
 * 
 * <p>
 * The Gaboto class represents the data model. It provides methods to interact
 * with the underlying RDF. This can be done either adding RDF directly or using
 * high-level objects such as {@link GabotoEntity} or
 * {@link GabotoTimeBasedEntity}.
 * </p>
 * 
 * <p>
 * It further more provides methods to query the data and to create
 * {@link GabotoSnapshot}s (containing a subset of the data).
 * </p>
 * 
 * <p>
 * A description of the underlying data model can be found at <a href="http://oxforderewhon.wordpress.com/2008/12/10/rdf-and-the-time-dimension-part-2/#more-201"
 * > RDF and the Time Dimension - Part 2</a>. Gaboto is using the solution with
 * named graphs rather than reification.
 * </p>
 * 
 * <p>
 * Gaboto objects cannot be instantiated directly. The {@link GabotoFactory} has
 * to be used.
 * </p>
 * 
 * @author Arno Mittelbach
 * @version 0.1
 * 
 * @see GabotoEntity
 * @see GabotoTimeBasedEntity
 * @see GabotoFactory
 */
public class Gaboto {

  private static Logger logger = Logger.getLogger(Gaboto.class.getName());

  /**
   * The next entity id.
   */
  long id = 23232322;

  /**
   * Stores update listeners.
   */
  private List<UpdateListener> updateListener = new ArrayList<UpdateListener>();

  /**
   * Stores named graph set.
   */
  private NamedGraphSet ngs;

  /**
	 * 
	 */
  private TimeDimensionIndexer timeIdx;

  private Model cdg;

  private GabotoConfiguration config;

  /**
   * Creates a new Gaboto object using the passed graphset.
   * 
   * <p>
   * Only accessible from within the package. Objects should be created via the
   * Gaboto factory.
   * </p>
   * 
   * @param graphset
   *          The data.
   */
  Gaboto(Model cdg, NamedGraphSet graphset) {
    this.ngs = graphset;
    this.cdg = cdg;
    this.config = GabotoLibrary.getConfig();
  }

  /**
   * Creates a new Gaboto object using the passed graphset for its data and
   * creating an index on the time dimension.
   * 
   * <p>
   * Only accessible from within the package. Objects should be created via the
   * Gaboto factory.
   * </p>
   * 
   * @param graphset
   *          The data.
   * @param idx
   *          A time dimension indexer.
   * 
   * @throws GabotoException
   */
  Gaboto(Model cdg, NamedGraphSet graphset, TimeDimensionIndexer idx)
      throws CorruptDataException {
    this.ngs = graphset;
    this.cdg = cdg;
    this.config = GabotoLibrary.getConfig();

    // create an index on the time dimension
    idx.createIndex(cdg);
    this.timeIdx = idx;
  }

  /**
   * Returns the time dimension indexer.
   * 
   * @return The time dimension indexer.
   * @throws NoTimeIndexSetException
   *           Thrown if no time dimension index was created.
   */
  public TimeDimensionIndexer getTimeDimensionIndexer()
      throws NoTimeIndexSetException {
    if (null == timeIdx)
      throw new NoTimeIndexSetException();
    return timeIdx;
  }

  /**
   * Sets a time dimension indexer.
   * 
   * @param idx
   *          The indexer.
   */
  public void setTimeDimensionIndexer(TimeDimensionIndexer idx) {
    this.timeIdx = idx;
  }

  /**
   * Triggers a recreation of the time dimension index.
   */
  public void recreateTimeDimensionIndex() {
    this.getTimeDimensionIndexer().createIndex(getContextDescriptionGraph());
  }

  /**
   * Attaches a listener that is informed on updates to the graph.
   * 
   * @param listener
   *          The update listener to be attached.
   */
  public void attachUpdateListener(UpdateListener listener) {
    updateListener.add(listener);
  }

  /**
   * Detaches an update listener.
   * 
   * @param listener
   *          The update listener to be detached.
   */
  public void detachUpdateListener(UpdateListener listener) {
    updateListener.remove(listener);
  }

  /**
   * Generates a new id unique within this Gaboto.
   * 
   * @return A new unique new.
   */
  public String generateID() {
    String id = generateId();
    while (containsResource(id))
      id = generateId();
    return id;
  }

  private String generateId() {
    return config.getNSData() + new Long(++id).toString();
  }

  /**
   * Creates a {@link GabotoSnapshot} that only contains flat RDF.
   * 
   * <p>
   * This method adds all information that was true at the given point in time
   * to the snapshot.
   * </p>
   * 
   * @param ti
   *          The time instant.
   * @return A snapshot only containing flat RDF.
   * 
   * @throws NoTimeIndexSetException
   * @throws CorruptTimeIndexException
   */
  public GabotoSnapshot getSnapshot(TimeInstant ti)
      throws NoTimeIndexSetException {
    logger.debug("create snapshot for time instant " + ti);
    Collection<String> graphURIs = getTimeDimensionIndexer()
        .getGraphsForInstant(ti);

    return getSnapshot(graphURIs);
  }

  /**
   * Creates an {@link GabotoSnapshot} that only contains the data from the
   * passed graph.
   * 
   * @param graph
   *          The graph that contains the data.
   * 
   * @return A snapshot.
   */
  public GabotoSnapshot getSnapshot(NamedGraph graph) {
    Collection<String> uris = new HashSet<String>();
    uris.add(graph.getGraphName().getURI());
    return getSnapshot(uris);
  }

  /**
   * Creates an {@link GabotoSnapshot} that only contains the data from the
   * passed graph.
   * 
   * @param graphURI
   *          The graph that contains the data.
   * 
   * @return A snapshot.
   */
  public GabotoSnapshot getSnapshot(String graphURI) {
    Collection<String> uris = new HashSet<String>();
    uris.add(graphURI);
    return getSnapshot(uris);
  }

  /**
   * Creates an {@link GabotoSnapshot} that contains the data from all the
   * specified graphs.
   * 
   * @param graphURIs
   *          The graphs to use.
   * 
   * @return A snapshot.
   */
  public GabotoSnapshot getSnapshot(Collection<String> graphURIs) {
    // create model
    Model model = ModelFactory.createDefaultModel();
    Graph newModelsDefaultGraph = model.getGraph();

    // create snapshot
    GabotoSnapshot snapshot = new GabotoSnapshot(model, this);

    // fill model
    for (String g : graphURIs) {
      logger.debug("add graph to snapshot: " + g);
      NamedGraph graph = ngs.getGraph(g);
      if (null == graph)
        throw new IllegalArgumentException("Unknown graph: " + g);

      // add statements to snapshot model
      ExtendedIterator it = graph.find(Node.ANY, Node.ANY, Node.ANY);
      while (it.hasNext()) {
        Triple t = (Triple)it.next();

        // add triple to model's graph
        newModelsDefaultGraph.add(t);
      }
    }

    // add gdg
    Graph gkg = getGlobalKnowledgeGraph();
    ExtendedIterator it = gkg.find(Node.ANY, Node.ANY, Node.ANY);
    while (it.hasNext())
      newModelsDefaultGraph.add((Triple)it.next());

    return snapshot;
  }

  /**
   * Informs listeners of update.
   * 
   * @param e
   */
  private void triggerUpdateEvent(GabotoEvent e) {
    for (UpdateListener u : updateListener)
      u.updateOccured(e);
  }

  /**
   * Adds a time bound entity to the data.
   * 
   * @param entityTB
   *          The entity to be added
   * @throws EntityAlreadyExistsException
   *           If the entity already exists in the model.
   */
  synchronized public void add(GabotoTimeBasedEntity entityTB)
      throws EntityAlreadyExistsException {
    if (containsEntity(entityTB.getUri()))
      throw new EntityAlreadyExistsException(entityTB.getUri());

    logger.debug("Adding time based entity to gaboto: " + entityTB);
    logger.debug("TimeSpans in tbEntity: " + entityTB.getTimeSpansSorted());

    // add triple denoting entities type and lifespan
    add(entityTB.getTimeSpan(), entityTB.getRDFTypeTriple());

    // loop over "internal" entities and add them
    Iterator<GabotoEntity> it = entityTB.iterator();
    while (it.hasNext()) {
      GabotoEntity entity = it.next();
      add(entity, false);
    }
  }

  /**
   * Changes an {@link GabotoEntity}. If entity did not exist, it is added to
   * the data.
   * 
   * <p>
   * Same as: <code>purge(entity); add(entity);</code>
   * </p>
   * 
   * @param entity
   *          The entity that is to be changed
   */
  synchronized public void change(GabotoEntity entity) {
    try {
      purge(entity);
      add(entity);
    } catch (EntityDoesNotExistException e) {
    } catch (EntityAlreadyExistsException e) {
      CorruptDataException cde = new CorruptDataException(
          "Something went teribly wrong .. I just purged " + entity.getUri()
              + ". It should not exist.");
      cde.initCause(e);
      throw cde;
    }
  }

  /**
   * Changes an {@link GabotoEntity}. If entity did not exist, it is added to
   * the data.
   * 
   * <p>
   * Same as: <code>purge(entity); add(entity);</code>
   * </p>
   * 
   * @param entity
   *          The entity that is to be changed
   */
  synchronized public void change(GabotoTimeBasedEntity entity) {
    try {
      purge(entity);
      add(entity);
    } catch (EntityDoesNotExistException e) {
    } catch (EntityAlreadyExistsException e) {
      CorruptDataException cde = new CorruptDataException(
          "Something went teribly wrong .. I just purged " + entity.getUri()
              + ". It should not exist.");
      cde.initCause(e);
      throw cde;
    }
  }

  /**
   * Adds an {@link GabotoEntity} to the data.
   * 
   * <p>
   * Same as: <code>add(entity, true)</code>.
   * </p>
   * 
   * @param entity
   *          The entity to be added.
   * 
   * @throws EntityAlreadyExistsException
   *           If the entity already exists in the model.
   * 
   * @see #add(GabotoEntity, boolean)
   */
  synchronized public void add(GabotoEntity entity)
      throws EntityAlreadyExistsException {
    add(entity, true);
  }

  /**
   * Adds an {@link GabotoEntity} to the data.
   * 
   * @param entity
   *          The entity to be added.
   * @param includeType
   *          Defines whether or not a triple that denotes the type should be
   *          added.
   * 
   * @throws EntityAlreadyExistsException
   *           If the entity already exists in the model.
   * 
   * @see #add(GabotoEntity, boolean)
   */
  synchronized private void add(GabotoEntity entity, boolean includeType)
      throws EntityAlreadyExistsException {
    if (containsEntity(entity) && includeType)
      throw new EntityAlreadyExistsException(entity);

    logger.debug("Adding entity to gaboto: " + entity);

    TimeSpan ts = entity.getTimeSpan().canonicalize();
    for (Triple t : entity.getTriplesFor(includeType))
      add(ts, t);
  }

  /**
   * Removes all direct information about the supplied entity.
   * 
   * @param entity
   *          The entity that is to be removed.
   * 
   * @throws EntityDoesNotExistException
   */
  synchronized public void purge(GabotoEntity entity)
      throws EntityDoesNotExistException {
    purge(entity.getUri());
  }

  /**
   * Removes all direct information about the supplied entity.
   * 
   * @param entity
   *          The entity that is to be removed.
   * 
   * @throws EntityDoesNotExistException
   */
  synchronized public void purge(GabotoTimeBasedEntity entity)
      throws EntityDoesNotExistException {
    purge(entity.getUri());
  }

  /**
   * Removes all direct information about the supplied entity.
   * 
   * @param entityURI
   *          The entity referenced by its URI.
   * 
   * @throws EntityDoesNotExistException
   */
  @SuppressWarnings("unchecked")
  synchronized public void purge(String entityURI)
      throws EntityDoesNotExistException {
    if (!containsEntity(entityURI))
      throw new EntityDoesNotExistException(entityURI);

    logger.debug("Attempting to purge " + entityURI);

    // load time-based entity
    Iterator it = getNamedGraphSet().findQuads(Node.ANY,
        Node.createURI(entityURI), Node.ANY, Node.ANY);
    while (it.hasNext()) {
      Quad q = (Quad)it.next();
      remove(q);

      // old way
      // TimeSpan ts = TimeSpan.createFromGraphName(q.getGraphName().getURI(),
      // this);
      // remove(ts, q.getTriple());
    }
  }

  /**
   * This method removes an entity from the graph described by its lifetime. For
   * more information see {@link #remove(GabotoEntity, TimeSpan)}.
   * 
   * @see #remove(GabotoEntity, TimeSpan)
   * @see #purge(GabotoEntity)
   * 
   * @param entity
   *          The entity to be removed.
   */
  synchronized public void remove(GabotoEntity entity) {
    remove(entity, entity.getTimeSpan());
  }

  /**
   * This method removes an entity from the graph described by the supplied
   * timespan.
   * 
   * <p>
   * Be careful, this method does not make sure that there are no other
   * references to this entity in other graphs. It may well be that other graphs
   * still contain triples that define properties for this entity. If you want
   * to remove all defining triples for this entity use
   * {@link #purge(GabotoEntity)}
   * </p>
   * 
   * @see #purge(GabotoEntity)
   * 
   * @param entity
   *          The entity to be removed
   * @param ts
   *          Defines the graph from which the entity should be removed.
   */
  synchronized public void remove(GabotoEntity entity, TimeSpan ts) {
    try {
      for (Triple t : entity.getTriplesFor(true))
        remove(ts, t);
    } catch (IllegalAnnotationException e) {
      e.printStackTrace();
    }
  }

  /**
   * Adds a triple to the GeneralKnowledgeGraph.
   * 
   * <p>
   * For more information about the GeneralKnowlegdeGraph see: <a href="http://oxforderewhon.wordpress.com/2008/12/10/rdf-and-the-time-dimension-part-2/#more-201"
   * > RDF and the Time Dimension - Part 2</a>.
   * <p>
   * 
   * <p>
   * Update listeners are informed.
   * </p>
   * 
   * @param triple
   *          The RDF triple to be added.
   * 
   * @see #attachUpdateListener(UpdateListener)
   * @see #detachUpdateListener(UpdateListener)
   * @see UpdateListener
   * @see GabotoInsertionEvent
   */
  synchronized public void add(Triple triple) {
    getGlobalKnowledgeGraph().add(triple);

    // inform listeners
    triggerUpdateEvent(new GabotoInsertionEvent(triple));
  }

  /**
   * Removes a triple from the GeneralKnowledgeGraph
   * 
   * <p>
   * For more information about the GeneralKnowlegdeGraph see: <a href="http://oxforderewhon.wordpress.com/2008/12/10/rdf-and-the-time-dimension-part-2/#more-201"
   * > RDF and the Time Dimension - Part 2</a>.
   * <p>
   * 
   * <p>
   * Update listeners are informed.
   * </p>
   * 
   * @param triple
   *          The RDF triple to be removed.
   * 
   * @see #attachUpdateListener(UpdateListener)
   * @see #detachUpdateListener(UpdateListener)
   * @see UpdateListener
   * @see GabotoRemovalEvent
   */
  synchronized public void remove(Triple triple) {
    getGlobalKnowledgeGraph().delete(triple);

    // inform listeners
    triggerUpdateEvent(new GabotoRemovalEvent(triple));
  }

  /**
   * Adds a triple to that graph corresponding to the supplied time span.
   * 
   * <p>
   * If this model does not yet contain a graph that corresponds to the supplied
   * time span, a new named graph is created.
   * </p>
   * 
   * <p>
   * Update listeners are informed.
   * </p>
   * 
   * @param ts
   *          The time span in which the triple is valid. if the time span is
   *          null, then the triple will be added to the gkg.
   * @param triple
   *          The information that is to be saved.
   * 
   * @return The NamedGraph the triple was added to.
   * 
   * @see #attachUpdateListener(UpdateListener)
   * @see #detachUpdateListener(UpdateListener)
   * @see UpdateListener
   * @see GabotoInsertionEvent
   */
  synchronized public NamedGraph add(TimeSpan ts, Triple triple) {
    if (null == ts || ts.equals(TimeUtils.EXISTANCE)) {
      add(triple);
      return null;
    }

    NamedGraph graph;

    if (containsGraph(ts))
      graph = getGraph(ts);
    else {
      graph = createNewGraph(ts);

      // update the index
      try {
        getTimeDimensionIndexer().add(graph, ts);
      } catch (NoTimeIndexSetException e) {
      }
    }

    logger.debug("Adding triple " + triple + " to graph "
        + graph.getGraphName().getURI());

    graph.add(triple);

    // inform listeners
    triggerUpdateEvent(new GabotoInsertionEvent(ts, triple));

    return graph;
  }

  /**
   * Removes an RDF triple from that graph corresponding to the supplied time
   * span.
   * 
   * <p>
   * Update listeners are informed.
   * </p>
   * 
   * @param ts
   *          The time span in which the triple is valid. if the time span is
   *          null, then the triple will be removed from the gkg.
   * @param triple
   *          The information that is to be removed.
   * 
   * @see #attachUpdateListener(UpdateListener)
   * @see #detachUpdateListener(UpdateListener)
   * @see UpdateListener
   * @see GabotoRemovalEvent
   */
  synchronized public void remove(TimeSpan ts, Triple triple) {
    if (null == ts || ts.equals(TimeUtils.EXISTANCE)) {
      remove(triple);
      return;
    }

    if (containsGraph(ts)) {
      NamedGraph graph = getGraph(ts);
      graph.delete(triple);

      triggerUpdateEvent(new GabotoRemovalEvent(ts, triple));
    }
  }

  /**
   * Removes a quad from Gaboto and triggers and update event.
   * 
   * @param q
   *          the quad.
   */
  synchronized public void remove(Quad q) {
    getNamedGraphSet().removeQuad(q);

    triggerUpdateEvent(new GabotoRemovalEvent(q));
  }

  /**
   * Returns the name of a potential graph for this timespan. The graph may or
   * may not exist in this instance of Gaboto.
   * 
   * @param ts
   *          The timespan/TimeInstant
   * @return the name a graph for this timespan would have
   */
  private String getGraphNameFor(TimeSpan ts) {
    return config.getNSGraphs() + "tg-" + ts.toString();
  }

  /**
   * Creates a new graph and sets all necessary triples in the CDG
   * 
   * @param ts
   *          the time span
   * 
   * @return The newly created named graph.
   */
  private NamedGraph createNewGraph(TimeSpan ts) {
    if (containsGraph(ts))
      throw new IllegalArgumentException("The graph for timespan " + ts
          + " already exists.");

    // calculate name and create graph
    String name = getGraphNameFor(ts);
    NamedGraph graph = getNamedGraphSet().createGraph(name);

    // put information about graph in cdg
    Model cdgModel = getContextDescriptionGraph();

    Graph cdgGraph = cdgModel.getGraph();

    // say that the graph is a graph. We do that in the cdg as well to have all
    // the information on the graph in the cdg
    cdgGraph.add(new Triple(Node.createURI(name), Node.createURI(RDF.type
        .getURI()), Node.createURI(RDFG.Graph.getURI())));

    // say that the graph is actually a graph
    cdgGraph.add(new Triple(Node.createURI(name), Node.createURI(RDF.type
        .getURI()), Node.createURI(RDFG.Graph.getURI())));

    // attach a temporal dimension to the graph
    Node tempD = Node.createAnon();
    cdgGraph.add(new Triple(Node.createURI(name), Node
        .createURI(RDFCON.hasTemporalDimension.getURI()), tempD));

    // Say that the temporal dimension is actually a Interval
    cdgGraph.add(new Triple(tempD, Node.createURI(RDF.type.getURI()), Node
        .createURI(TimeVocab.Interval.getURI())));

    // attach a beginning to the interval
    Node beginning = Node.createAnon();
    cdgGraph.add(new Triple(tempD, Node.createURI(TimeVocab.hasBeginning
        .getURI()), beginning));

    // say that the beginning is an instant
    cdgGraph.add(new Triple(beginning, Node.createURI(RDF.type.getURI()), Node
        .createURI(TimeVocab.Instant.getURI())));

    // attach a time description to the beginning
    Node beginningDesc = Node.createAnon();
    cdgGraph.add(new Triple(beginning, Node
        .createURI(TimeVocab.hasDateTimeDescription.getURI()), beginningDesc));

    // set the type of the time description
    cdgGraph.add(new Triple(beginningDesc, Node.createURI(RDF.type.getURI()),
        Node.createURI(TimeVocab.DateTimeDescription.getURI())));

    // now actually describe the time description
    String unitType = "";
    if (ts.getStartUnit() == TimeSpan.START_UNIT_DAY)
      unitType = TimeVocab.unitDay.getURI();
    else if (ts.getStartUnit() == TimeSpan.START_UNIT_MONTH)
      unitType = TimeVocab.unitMonth.getURI();
    else
      unitType = TimeVocab.unitYear.getURI();
    cdgGraph.add(new Triple(beginningDesc, Node.createURI(TimeVocab.unitType
        .getURI()), Node.createURI(unitType)));
    cdgGraph.add(new Triple(beginningDesc, Node.createURI(TimeVocab.year
        .getURI()), Node.createLiteral(String.valueOf(ts.getStartYear()), null,
        XSDDatatype.XSDinteger)));
    if (null != ts.getStartMonth()) {
      cdgGraph.add(new Triple(beginningDesc, Node.createURI(TimeVocab.month
          .getURI()), Node.createLiteral(String.valueOf(ts.getStartMonth()),
          null, XSDDatatype.XSDinteger)));
    }
    if (null != ts.getStartDay()) {
      cdgGraph.add(new Triple(beginningDesc, Node.createURI(TimeVocab.day
          .getURI()), Node.createLiteral(String.valueOf(ts.getStartDay()),
          null, XSDDatatype.XSDinteger)));
    }

    // / duration

    if (ts.hasFixedDuration()) {
      Node durationDesc = Node.createAnon();
      cdgGraph.add(new Triple(tempD, Node
          .createURI(TimeVocab.hasDurationDescription.getURI()), durationDesc));
      cdgGraph.add(new Triple(durationDesc, Node.createURI(RDF.type.getURI()),
          Node.createURI(TimeVocab.DurationDescription.getURI())));

      if (null != ts.getDurationYear()) {
        cdgGraph
            .add(new Triple(durationDesc, Node.createURI(TimeVocab.years
                .getURI()), Node.createLiteral(String.valueOf(ts
                .getDurationYear()), null, XSDDatatype.XSDinteger)));
      }
      if (null != ts.getDurationMonth()) {
        cdgGraph.add(new Triple(durationDesc, Node.createURI(TimeVocab.months
            .getURI()), Node.createLiteral(String
            .valueOf(ts.getDurationMonth()), null, XSDDatatype.XSDinteger)));
      }
      if (null != ts.getDurationDay()) {
        cdgGraph.add(new Triple(durationDesc, Node.createURI(TimeVocab.days
            .getURI()), Node.createLiteral(String.valueOf(ts.getDurationDay()),
            null, XSDDatatype.XSDinteger)));
      }
    }

    return graph;
  }

  /**
   * Returns the underlying named graph set.
   * 
   * @return The named graph set.
   */
  public NamedGraphSet getNamedGraphSet() {
    return this.ngs;
  }

  /**
   * Tests if this Gaboto instance knows of a graph described by this timespan
   * object
   * 
   * @param ts
   *          The time span.
   * @return True if Gaboto contains a graph for this time span
   */
  public boolean containsGraph(TimeSpan ts) {
    return this.getNamedGraphSet().containsGraph(getGraphNameFor(ts));
  }

  /**
   * Tests if Gaboto contains a graph with the passed URI.
   * 
   * @param uri
   *          The graph's name.
   * 
   * @return True if Gaboto contains the graph.
   */
  public boolean containsGraph(String uri) {
    return this.getNamedGraphSet().containsGraph(uri);
  }

  /**
   * Returns the named graph or null.
   * 
   * @param uri
   *          The graph's name.
   * 
   * @return The named graph or null.
   */
  public NamedGraph getGraph(String uri) {
    return this.getNamedGraphSet().getGraph(uri);
  }

  /**
   * Returns the corresponding graph.
   * 
   * @param ts
   *          The time span defining the graph.
   * 
   * @return The corresponding graph or null.
   */
  public NamedGraph getGraph(TimeSpan ts) {
    return this.getNamedGraphSet().getGraph(getGraphNameFor(ts));
  }

  /**
   * Returns the global knowledge graph.
   * 
   * <p>
   * For more information about the global knowledge graph see <a href="http://oxforderewhon.wordpress.com/2008/12/10/rdf-and-the-time-dimension-part-2/"
   * > RDF and the Time Dimension - Part 2</a>.
   * </p>
   * 
   * @return The global knowledge graph.
   */
  public NamedGraph getGlobalKnowledgeGraph() {
    return ngs.getGraph(config.getGKG());
  }

  /**
   * Returns the context description graph.
   * 
   * <p>
   * For more information about the context description graph see <a href="http://oxforderewhon.wordpress.com/2008/12/10/rdf-and-the-time-dimension-part-2/"
   * > RDF and the Time Dimension - Part 2</a>.
   * </p>
   * 
   * @return The context description graph.
   */
  public Model getContextDescriptionGraph() {
    return cdg;
  }

  /**
   * Returns a Jena Model view on the underlying NamedGraphSet.
   * 
   * <p>
   * For more information see the <a
   * href="http://www4.wiwiss.fu-berlin.de/bizer/ng4j/">ng4j website</a> and <a
   * href="http://www4.wiwiss.fu-berlin.de/bizer/ng4j/javadoc/index.html?de/fuberlin/wiwiss/ng4j/NamedGraphSet.html"
   * >ng4j's javadoc</a>.
   * </p>
   * 
   * @return A Jena Model view on the underlying NamedGraphSet.
   */
  public Model getJenaModelViewOnNamedGraphSet() {
    return getNamedGraphSet().asJenaModel(config.getDefaultGraph());
  }

  /**
   * Tests whether the passed entity exists.
   * 
   * <p>
   * It is not tested whether the system has a copy of the entity in that
   * particular form (all the attributes set to specific values). It will just
   * be checked if there is a triple in the system that assigns the entity an
   * RDF type:
   * 
   * <pre>
   * uri		rdf:type		?t .
   * </pre>
   * 
   * </p>
   * 
   * @param entity
   *          The entity to test.
   * 
   * @return True, if there is some information about this entity stored in the
   *         system.
   */
  public boolean containsEntity(GabotoEntity entity) {
    return containsEntity(entity.getUri());
  }

  /**
   * Tests whether the passed entity exists.
   * 
   * <p>
   * It is not tested whether the system has a copy of the entity in that
   * particular form (all the attributes set to specific values). It will just
   * be checked if there is a triple in the system that assigns the entity an
   * RDF type:
   * 
   * <pre>
   * uri		rdf:type		?t .
   * </pre>
   * 
   * </p>
   * 
   * @param uri
   *          The entity's URI.
   * 
   * @return True, if there is some information about this entity stored in the
   *         system.
   */
  public boolean containsEntity(String uri) {
    if (null == uri)
      throw new IllegalArgumentException("URI may not be null.");
    return getNamedGraphSet().containsQuad(
        new Quad(Node.ANY, Node.createURI(uri), Node.createURI(RDF.type
            .getURI()), Node.ANY));
  }

  /**
   * Loads an entity at a given point in time.
   * 
   * @param uri
   *          The entity's URI.
   * @param ti
   *          The time instant.
   * 
   * @return The entity.
   * 
   * @throws EntityDoesNotExistException
   * @throws NoTimeIndexSetException
   */
  public GabotoEntity getEntity(String uri, TimeInstant ti)
      throws EntityDoesNotExistException {
    if (!containsEntity(uri))
      throw new EntityDoesNotExistException(uri);

    GabotoSnapshot snap = getSnapshot(ti);

    try {
      return snap.loadEntity(uri);
    } catch (ResourceDoesNotExistException e) {
      e.printStackTrace();
    }

    // this point should not be reached
    assert (false);

    return null;
  }

  /**
   * Loads an entity with all its data.
   * 
   * @param uri
   *          The entity's URI.
   * @return A full representation of the entity.
   * @throws EntityDoesNotExistException
   */
  public GabotoTimeBasedEntity getEntityOverTime(String uri)
      throws EntityDoesNotExistException {
    if (!containsEntity(uri))
      throw new EntityDoesNotExistException(uri);

    try {
      return GabotoTimeBasedEntity.loadEntity(uri, this);
    } catch (ResourceDoesNotExistException e) {
      e.printStackTrace();
    }

    // this point should never be reached
    assert (false);

    return null;
  }

  /**
   * Tests whether the system contains data about the specified resource.
   * 
   * <p>
   * It is tested if there is any triple that has the resource's URI as its
   * subject.
   * </p>
   * 
   * @param res
   *          The resource.
   * 
   * @return True, if the resource exists.
   */
  public boolean containsResource(Resource res) {
    return containsResource(res.getURI());
  }

  /**
   * Tests whether the system contains data about the specified resource.
   * 
   * <p>
   * It is tested if there is any triple that has the resource's URI as its
   * subject.
   * </p>
   * 
   * @param uri
   *          The resource's URI.
   * 
   * @return True, if the resource exists.
   */
  public boolean containsResource(String uri) {
    return getNamedGraphSet().containsQuad(
        new Quad(Node.ANY, Node.createURI(uri), Node.ANY, Node.ANY));
  }

  /**
   * Tries to figure out the ontology type of an entity identified by its URI.
   * 
   * @param uri
   *          The entity's URI.
   * 
   * @throws CorruptDataException
   * @throws EntityDoesNotExistException
   */
  @SuppressWarnings("unchecked")
  public String getTypeOf(String uri) throws CorruptDataException,
      EntityDoesNotExistException {
    if (!containsEntity(uri))
      throw new EntityDoesNotExistException(uri);

    Iterator it = getNamedGraphSet().findQuads(Node.ANY, Node.createURI(uri),
        Node.createURI(RDF.type.getURI()), Node.ANY);

    if (it.hasNext()) {
      Quad quad = (Quad)it.next();
      if (it.hasNext())
        logger.error("Corrupted data. " + uri
            + " has to triples defining its type");

      if (!quad.getObject().isURI()) {
        logger.error("Corrupted data. " + uri + " has has not a valid type.");
        throw new CorruptDataException("Corrupted data. " + uri
            + " has has not a valid type.");
      }

      String name = quad.getObject().getLocalName();
      String packageName = GabotoEntity.class.getPackage().getName();

      // try and load type
      try {
        Class<?> clazz = Class.forName(packageName + "." + name);
        Object object = clazz.newInstance();
        if (!(object instanceof GabotoEntity))
          throw new CorruptDataException("Type for uri " + uri
              + " should point to an GabotoEntity.");

        // cast object
        GabotoEntity entity = (GabotoEntity)object;

        // return type
        return entity.getType();
      } catch (ClassNotFoundException e) {
        logger.error("Could not find associated type for uri: " + uri
            + ". Tried to load class " + packageName + "." + name);
      } catch (InstantiationException e) {
        CorruptDataException e1 = new CorruptDataException(
            "Could not instantiate class " + packageName + "." + name);
        e1.initCause(e);
        throw e1;
      } catch (IllegalAccessException e) {
        CorruptDataException e1 = new CorruptDataException(
            "Could not instantiate class " + packageName + "." + name);
        e1.initCause(e);
        throw e1;
      }
    }

    // let's hope we will never come here
    logger.fatal("We should never have reached this point.");
    assert (false);

    return null;
  }

  /**
   * Returns an entity's life time.
   * 
   * <p>
   * The life time of an entity is described by the graph that contains its type
   * definition.
   * </p>
   * 
   * @param uri
   *          The entity's URI
   * 
   * @return The life time
   * 
   * @throws CorruptDataException
   * @throws EntityDoesNotExistException
   */
  @SuppressWarnings("unchecked")
  public TimeSpan getEntitysLifetime(String uri) throws CorruptDataException,
      EntityDoesNotExistException {
    if (!containsEntity(uri))
      throw new EntityDoesNotExistException(uri);

    Iterator it = getNamedGraphSet().findQuads(Node.ANY, Node.createURI(uri),
        Node.createURI(RDF.type.getURI()), Node.ANY);

    if (it.hasNext()) {
      Quad quad = (Quad)it.next();
      if (it.hasNext())
        logger.error("Corrupted data. " + uri
            + " has to triples defining its type");

      if (!quad.getObject().isURI()) {
        logger.error("Corrupted data. " + uri + " has has not a valid type.");
        throw new CorruptDataException("Corrupted data. " + uri
            + " has has not a valid type.");
      }

      return TimeSpan.createFromGraphName(quad.getGraphName().getURI(), this);
    }

    // let's hope we will never come here
    logger.fatal("We should never have reached this point.");
    assert (false);

    return null;
  }

  /**
   * Returns a list of URIs for entities that have the specified property.
   * 
   * @param prop
   *          The property
   * 
   * @return A collection of uris.
   */
  public Collection<String> getEntityURIsFor(Property prop) {
    Collection<String> uris = new HashSet<String>();

    // find in named graphs
    Iterator<?> it = getNamedGraphSet().findQuads(Node.ANY, Node.ANY,
        Node.createURI(prop.getURI()), Node.ANY);
    while (it.hasNext()) {
      Quad q = (Quad)it.next();
      uris.add(q.getSubject().getURI());
    }

    return uris;
  }

  /**
   * Returns a list of URIs for entities that have the specified property.
   * 
   * @param prop
   *          The property
   * @param value
   *          The property's value
   * 
   * @return A collection of uris.
   */
  public Collection<String> getEntityURIsFor(Property prop, String value) {
    Collection<String> uris = new HashSet<String>();

    // find in named graphs
    Iterator<?> it = getNamedGraphSet().findQuads(Node.ANY, Node.ANY,
        Node.createURI(prop.getURI()), Node.createLiteral(value));
    while (it.hasNext()) {
      Quad q = (Quad)it.next();
      uris.add(q.getSubject().getURI());
    }

    return uris;
  }

  /**
   * Returns a list of URIs for entities that have the specified property.
   * 
   * @param prop
   *          The property
   * @param value
   *          The property's value
   * 
   * @return A collection of uris.
   */
  public Collection<String> getEntityURIsFor(Property prop, Node value) {
    Collection<String> uris = new HashSet<String>();

    // find in named graphs
    Iterator<?> it = getNamedGraphSet().findQuads(Node.ANY, Node.ANY,
        Node.createURI(prop.getURI()), value);
    while (it.hasNext()) {
      Quad q = (Quad)it.next();
      uris.add(q.getSubject().getURI());
    }

    return uris;
  }

  /**
   * Loads entities that have a certain property.
   * 
   * @param prop
   *          The property
   * @return An entity pool with all entities that have this property.
   */
  public Collection<GabotoTimeBasedEntity> loadEntitiesOverTimeWithProperty(
      Property prop) {
    Collection<GabotoTimeBasedEntity> entities = new HashSet<GabotoTimeBasedEntity>();

    for (String uri : getEntityURIsFor(prop)) {
      try {
        entities.add(getEntityOverTime(uri));
      } catch (EntityDoesNotExistException e) {
        e.printStackTrace();
      }
    }

    return entities;
  }

  /**
   * Loads entities that have a certain property.
   * 
   * @param prop
   *          The property
   * @param value
   *          The property's value.
   * 
   * @return An entity pool with all entities that have this property/value
   *         pair.
   */
  public Collection<GabotoTimeBasedEntity> loadEntitiesOverTimeWithProperty(
      Property prop, String value) {
    Collection<GabotoTimeBasedEntity> entities = new HashSet<GabotoTimeBasedEntity>();

    for (String uri : getEntityURIsFor(prop, value)) {
      try {
        entities.add(getEntityOverTime(uri));
      } catch (EntityDoesNotExistException e) {
        e.printStackTrace();
      }
    }

    return entities;
  }

  /**
   * Loads entities that have a certain property.
   * 
   * @param prop
   *          The property
   * @param value
   *          The property's value.
   * 
   * @return An entity pool with all entities that have this property/value
   *         pair.
   */
  public Collection<GabotoTimeBasedEntity> loadEntitiesOverTimeWithProperty(
      Property prop, Node value) {
    Collection<GabotoTimeBasedEntity> entities = new HashSet<GabotoTimeBasedEntity>();

    for (String uri : getEntityURIsFor(prop, value)) {
      try {
        entities.add(getEntityOverTime(uri));
      } catch (EntityDoesNotExistException e) {
        e.printStackTrace();
      }
    }

    return entities;
  }

  /**
   * 
   * @param os
   *          The OutputStream to write to.
   */
  public void write(OutputStream os) {
    getNamedGraphSet().write(os, "TRIG", null);
  }

  /**
   * 
   * @param os
   *          The OutputStream to write to.
   * @param format
   *          The output format to use.
   */
  public void write(OutputStream os, String format) {
    getNamedGraphSet().write(os, format, null);
  }

  /**
   * 
   * @param os
   *          The OutputStream to write to.
   */
  public void writeCDG(OutputStream os) {
    getContextDescriptionGraph().write(os);
  }

  /**
   * 
   * @param os
   *          The OutputStream to write to.
   * @param format
   *          The RDF format.
   */
  public void writeCDG(OutputStream os, String format) {
    getContextDescriptionGraph().write(os, format);
  }

  public void read(InputStream oxpIS, InputStream cdgIS) {
    if (oxpIS == null)
      throw new NullPointerException();
    if (cdgIS == null)
      throw new NullPointerException();
    getNamedGraphSet().read(oxpIS, "TRIG", null);
    getContextDescriptionGraph().read(cdgIS, "RDF/XML");
  }

  /**
   * 
   * @param oxpIS
   * @param oxpFormat
   * @param cdgIS
   * @param cdgFormat
   */
  public void read(InputStream oxpIS, String oxpFormat, InputStream cdgIS,
      String cdgFormat) {
    getNamedGraphSet().read(oxpIS, oxpFormat, null);
    getContextDescriptionGraph().read(cdgIS, cdgFormat);
  }

}
