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
package org.oucs.gaboto.model.query;

import org.oucs.gaboto.entities.pool.GabotoEntityPool;
import org.oucs.gaboto.entities.pool.GabotoEntityPoolConfiguration;
import org.oucs.gaboto.model.Gaboto;
import org.oucs.gaboto.model.GabotoSnapshot;
import org.oucs.gaboto.time.TimeInstant;

/**
 * Simple query that grabs all entities of a specific type.
 * 
 * 
 * @author Arno Mittelbach
 * @version 0.1
 */
public class ListOfTypedEntities extends GabotoQueryImpl {
	
	private String type;
	private TimeInstant timeInstant;
	private boolean forceCreation;

	public ListOfTypedEntities(String type, TimeInstant ti) {
		super();
		this.type = type;
		this.timeInstant = ti;
		this.forceCreation = true;
	}

	public ListOfTypedEntities(String type, TimeInstant ti, boolean forceCreation) {
		super();
		this.type = type;
		this.timeInstant = ti;
		this.forceCreation = forceCreation;
	}
	
	public ListOfTypedEntities(Gaboto gaboto, String type, TimeInstant ti) {
		super(gaboto);
		this.type = type;
		this.timeInstant = ti;
		this.forceCreation = true;
	}

	public ListOfTypedEntities(Gaboto gaboto, String type, TimeInstant ti, boolean forceCreation) {
		super(gaboto);
		this.type = type;
		this.timeInstant = ti;
		this.forceCreation = forceCreation;
	}
	
	
	@Override
	public int getResultType() {
		return GabotoQueryImpl.RESULT_TYPE_ENTITY_POOL;
	}

	@Override
	public Object execute() {
		// create snapshot
		GabotoSnapshot snapshot = getGaboto().getSnapshot(timeInstant);
		// create config
		GabotoEntityPoolConfiguration config = new GabotoEntityPoolConfiguration(snapshot);
		config.addAcceptedType(type);
		
  	return GabotoEntityPool.createFrom(config);
	}

	@Override
	protected void doPrepare() {
	  // Fool Eclipse
	  if (forceCreation) 
	    forceCreation = true;
	}
	
}
