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
package org.oucs.gaboto.beans;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oucs.gaboto.entities.GabotoEntity;
import org.oucs.gaboto.entities.pool.GabotoEntityPool;
import org.oucs.gaboto.entities.utils.GabotoEntityUtils;
import org.oucs.gaboto.exceptions.GabotoRuntimeException;
import org.oucs.gaboto.model.GabotoSnapshot;
import org.oucs.gaboto.reflection.RDFContainer;
import org.oucs.gaboto.reflection.RDFContainerTripleGeneratorImpl;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * GabotoBeans are used to allow the storage of <a
 * href="http://www.w3.org/TR/REC-rdf-syntax/#structuredproperties">complex
 * properties</a> in {@link GabotoEntity}s.
 * 
 * <p>
 * GabotoBeans should generally be simple classes following the principles of
 * JavaBeans (see <a href="http://en.wikipedia.org/wiki/JavaBeans">Wikipedia
 * article</a>). The only two a bit more complex methods an GabotoBean has to
 * provide is one method to generate RDF Triples (
 * {@link #getCorrespondingRDFTriples(Node)} and one to load itself from an RDF
 * resource ({@link #loadFromResource(Resource)}).
 * </p>
 * 
 * 
 * @author Arno Mittelbach
 */
public abstract class GabotoBean implements RDFContainer {

  /**
   * 
   * @return the Type
   */
  abstract public String getType();

  /**
   * Create RDF triples that contain all the information stored in this object.
   * 
   * <p>
   * RDF triples for {@link GabotoBean}s will always be of the form:
   * 
   * <pre>
   *  oxpdata:subject		somevocab:definedPredicate		_blankNode .
   *  _blankNode			somevocab:predicate1		 	&quot;some value&quot;;
   *  					somevocab:predicate2		 	someReference;
   *  						...								...		 .
   * </pre>
   * 
   * The first triple is created automatically. It is the beans obligation to
   * create all other necessary "information triples".
   * </p>
   * 
   * @param blankNode
   *          The blank node to attach information to.
   * @return A list of RDF triples.
   */
  public List<Triple> getCorrespondingRDFTriples(Node blankNode) {
    List<Triple> triples = RDFContainerTripleGeneratorImpl.getInstance()
        .getTriplesFor(this, blankNode);
    return triples;
  }

  /**
   * Load all information necessary for this {@link GabotoBean} from an RDF
   * resource.
   * 
   * <p>
   * In order to automatically load {@link GabotoEntity}s beans have to know how
   * their information is stored in RDF and be able to load this information
   * into its fields.
   * </p>
   * 
   * @param snapshot
   * @param pool
   * @param res
   *          The RDF resource containing the information to load this
   *          {@link GabotoBean}.
   */
  public void loadFromResource(Resource res, GabotoSnapshot snapshot,
      GabotoEntityPool pool) {
    // load bean
    // FIXME Why commented out?
    // throw new RuntimeException("Not yet implemented");
    // RDFContainerLoaderImpl.getInstance().loadFromSnapshot(this, res,
    // snapshot, pool);
  }

  /**
   * Returns the value of a property via reflection
   * 
   * @param prop
   *          The property
   * @return The property's value (or null).
   */
  public Object getPropertyValue(Property prop) {
    return getPropertyValue(prop.getURI());
  }

  public Object getPropertyValue(String propURI) {
    Method method = GabotoEntityUtils.getBeanGetMethodFor(this.getClass(),
        propURI);
    if (null != method) {
      try {
        return method.invoke(this, (Object[]) null);
      } catch (Exception e) {
        throw new GabotoRuntimeException(e);
      }
    }

    return null;
  }

  public Map<String, Object> getAllProperties() {
    Map<String, Object> properties = new HashMap<String, Object>();

    for (String prop : GabotoEntityUtils.getAllBeanProperties(this.getClass())) {
      properties.put(prop, getPropertyValue(prop));
    }

    return properties;
  }
}
