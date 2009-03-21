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
package org.oucs.gaboto.sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.oucs.gaboto.beans.Location;
import org.oucs.gaboto.entities.College;
import org.oucs.gaboto.entities.GabotoEntity;
import org.oucs.gaboto.entities.pool.GabotoEntityPool;
import org.oucs.gaboto.entities.pool.GabotoEntityPoolConfiguration;
import org.oucs.gaboto.exceptions.GabotoException;
import org.oucs.gaboto.model.Gaboto;
import org.oucs.gaboto.model.GabotoSnapshot;
import org.oucs.gaboto.model.query.GabotoQueryImpl;
import org.oucs.gaboto.timedim.TimeInstant;
import org.oucs.gaboto.vocabulary.OxPointsVocab;

import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DC_11;

public class CollegesNearEntity extends GabotoQueryImpl {

	private String title;
	private TimeInstant timeInstant;
	private int number;
	
	private GabotoSnapshot snapshot;
	
	private List<College> listOfColleges;
	
	public CollegesNearEntity(String title, int number, TimeInstant ti) throws GabotoException {
		super();
		this.title = title;
		this.timeInstant = ti;
		this.number = number;
	}
	
	public CollegesNearEntity(Gaboto gaboto, String title, int number, TimeInstant ti) throws GabotoException {
		super(gaboto);
		this.title = title;
		this.timeInstant = ti;
		this.number = number;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	@Override
	protected void doPrepare() throws GabotoException {
		// create snapshot
		snapshot = getGaboto().getSnapshot(timeInstant);
		
		// get all colleges
		GabotoEntityPoolConfiguration config = new GabotoEntityPoolConfiguration(snapshot);
		config.addAcceptedType(OxPointsVocab.College_URI);
		GabotoEntityPool colleges = GabotoEntityPool.createFrom(config);
		
		// find colleges we are interested in
		listOfColleges = new ArrayList<College>();
		for(GabotoEntity en : colleges.getEntities()){
			// cast to college
			College col = (College) en;
			
			// get its location
			Location colLoc = (Location) col.getPropertyValue(OxPointsVocab.hasLocation);
			if(null == colLoc)
				continue;
			
			// add to results if not enough results yet
			listOfColleges.add(col);
		}
	}
	
	@Override
	public int getResultType() {
		return GabotoQueryImpl.RESULT_TYPE_ENTITY_POOL;
	}

	@Override
	protected Object execute() throws GabotoException {
		// find entity with name
		GabotoEntityPool entities = snapshot.loadEntitiesWithProperty(DC_11.title, title);
		Iterator<GabotoEntity> it = entities.getEntities().iterator();
		if(! it.hasNext())
			throw new IllegalArgumentException("There is no entity with that title.");
		GabotoEntity entity = it.next();
		
		// we do not know what kind of entity we have
		// but we are interested in its location. We
		// can get to this property, by calling its getPropertyValue
		// method.
		Location loc = (Location) entity.getPropertyValue(OxPointsVocab.hasLocation);
		if(null == loc)
			throw new IllegalArgumentException("The entity " + entity.getUri() + " does not have a location.");
		
		// store latitude and longitude
		final double lat = loc.getLatitude();
		final double _long = loc.getLongitude();
		
		// sort results
		Collections.sort(listOfColleges, new Comparator<College>(){

			public int compare(College c1, College c2) {
				// distance of college 1
				Location loc = (Location) c1.getPropertyValue(OxPointsVocab.hasLocation);
				double distX = Math.abs(lat - loc.getLatitude());
				double distY = Math.abs(_long - loc.getLongitude());
				double dis1 = Math.sqrt(distX*distX + distY*distY);
				
				// distance of college 2
				loc = (Location) c2.getPropertyValue(OxPointsVocab.hasLocation);
				distX = Math.abs(lat - loc.getLatitude());
				distY = Math.abs(_long - loc.getLongitude());
				double dis2 = Math.sqrt(distX*distX + distY*distY);
				
				if(dis1 < dis2)
					return -1;
				else if(dis1 > dis2)
					return 1;
				
				return 0;
			}
			
		});
		
		//create result pool
		GabotoEntityPool resultPool = new GabotoEntityPool(getGaboto(), snapshot);
		for(int i = 0; i < number && i < listOfColleges.size(); i++)
			resultPool.addEntity(listOfColleges.get(i));
		
		return resultPool;
	}


}
