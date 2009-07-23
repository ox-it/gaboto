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
package org.oucs.gaboto.entities.pool.filters;

import org.oucs.gaboto.entities.pool.GabotoEntityPool;
import org.oucs.gaboto.entities.pool.GabotoEntityPoolConfiguration;
import org.oucs.gaboto.nodes.GabotoEntity;

/**
 * EntityFilters can be used to guide the creation process of {@link GabotoEntityPool}s. 
 * 
 * <p>
 * EntityFilters are an easy to use high-level tool to filter the entities that are added to an
 * {@link GabotoEntityPool}. However, the filters are only applied after the entity was created,
 * thereby having a slight drawback on the performance in contrast to, for example,
 * (properly written) SPARQL filters. 
 * </p>
 * 
 * @author Arno Mittelbach
 * @version 0.1
 * @see GabotoEntityPoolConfiguration
 * @see GabotoEntityPool
 */
public abstract class EntityFilter {

	
	/**
	 * Defines to what entity type this filter applies to.
	 * 
	 * @see GabotoEntity#getType()
	 * @return To what entity type this filter applies to.
	 */
	public Class<? extends GabotoEntity> appliesTo(){
		return GabotoEntity.class;
	}
	
	/**
	 * Performs the actual filter operation.
	 * 
	 * <p>
	 * Returns false if the entity did not pass the filter criteria. True otherwise.
	 * </p>
	 * 
	 * @param entity The entity to be filtered.
	 * @return false|true (reject|pass). 
	 */
	abstract public boolean filterEntity(GabotoEntity entity);
}
