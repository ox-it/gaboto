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
package net.sf.gaboto.node;

import java.util.Iterator;
import java.util.List;

import net.sf.gaboto.time.TimeInstant;
import net.sf.gaboto.time.TimeSpan;

/**
 * Iterates over a timeBased entity returning GabotoEntity objects in the correct order (sorted by time).
 * 
 * <p>
 * Does not implement remove.
 * </p>
 * 
 * @author Arno Mittelbach
 * @version 0.1
 *
 */
public class TimeBasedEntityIterator implements Iterator<GabotoEntity> {

	private GabotoTimeBasedEntity tbEntity;
	private GabotoEntity entity;

	private List<TimeSpan> timespans;
	
	private int index; 
	
	public TimeBasedEntityIterator(GabotoTimeBasedEntity tbEntity){
		this.tbEntity = tbEntity;
		timespans = tbEntity.getTimeSpansSorted();
		
		index = 0;
		this.entity = tbEntity.getEntity(timespans.get(index).getBegin());
		index++;
	}
	
	public boolean hasNext() {
		return null != this.entity;
	}

	public GabotoEntity next() {
		GabotoEntity tmp = entity;
		
		try{
			TimeInstant begin = timespans.get(index).getBegin();
			if(tmp == null)
				System.out.println("lala");
			
			while(entity.getTimeSpan().getBegin().canUnify(begin)){
				index++;
				begin = timespans.get(index).getBegin();
			}
			entity = tbEntity.getEntity(begin);
			index++;
		} catch(IndexOutOfBoundsException e){
			entity = null;
		}
		
		return tmp;
	}

	public void remove() {
		// not implemented
	}

}
