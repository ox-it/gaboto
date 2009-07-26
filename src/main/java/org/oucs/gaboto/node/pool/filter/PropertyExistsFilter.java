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
package org.oucs.gaboto.node.pool.filter;

import org.oucs.gaboto.node.GabotoEntity;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Filter to be used in creation of entity pool to ensure that each resource
 * this filter applies to does not have a certain property.
 * 
 * @author Arno Mittelbach
 * @version 0.1
 */
public class PropertyExistsFilter extends ResourceFilter {

  private Property property;
  private Class<? extends GabotoEntity> appliesTo;

  /**
   * Instantiates a new filter with the supplied property that applies to all
   * entity types.
   * 
   * @param property
   *          The property to look for.
   */
  public PropertyExistsFilter(Property property) {
    this.property = property;
    this.appliesTo = GabotoEntity.class;
  }

  /**
   * Instantiates a new filter with the supplied property that applies to the
   * passed entity types.
   * 
   * @param property
   *          The property to look for.
   * @param appliesTo
   *          The entity type this filter applies to.
   */
  public PropertyExistsFilter(Property property,
      Class<? extends GabotoEntity> appliesTo) {
    this.property = property;
    this.appliesTo = appliesTo;
  }

  @Override
  public Class<? extends GabotoEntity> appliesTo() {
    return appliesTo;
  }

  @Override
  public boolean filterResource(Resource res) {
    return res.hasProperty(property);
  }

}
