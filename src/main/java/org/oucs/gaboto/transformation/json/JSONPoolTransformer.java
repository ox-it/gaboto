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
package org.oucs.gaboto.transformation.json;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONStringer;
import org.oucs.gaboto.beans.GabotoBean;
import org.oucs.gaboto.entities.GabotoEntity;
import org.oucs.gaboto.entities.pool.GabotoEntityPool;
import org.oucs.gaboto.exceptions.GabotoRuntimeException;
import org.oucs.gaboto.transformation.EntityPoolTransformer;

/**
 * Transforms a GabotoEntityPool into JSON.
 * 
 * @author Arno Mittelbach
 *
 */
public class JSONPoolTransformer implements EntityPoolTransformer {

	private Map<GabotoEntity, Integer> levelMap = new HashMap<GabotoEntity, Integer>();
	
	/**
	 * Collect all direct properties
	 */
	public static final int COLLECT_DIRECT_PROPERTIES = 1;
	
	/**
	 * Collect all passive properties
	 */
	public static final int COLLECT_PASSIVE_PROPERTIES = 2;
	
	/**
	 * Collect all indirect properties
	 */
	public static final int COLLECT_INDIRECT_PROPERTIES = 4;
	
	/**
	 * The maximum allowed nesting level
	 */
	public static final int MAX_NESTING = 8;
	
	private int nesting = 1;
	private int collectProperties = COLLECT_DIRECT_PROPERTIES | COLLECT_INDIRECT_PROPERTIES | COLLECT_PASSIVE_PROPERTIES;
	
	public String transform(GabotoEntityPool pool) {
			return transfromEntities(pool.getEntities());
	}

    private String simplifyKey(String k) { 
	k = k.replaceAll("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#", "oxp_");
	k = k.replaceAll("http://ns.ox.ac.uk/namespace/gaboto/kml/2009/03/owl#", "gab_");
	k = k.replaceAll("http://purl.org/dc/elements/1.1/", "dc_");
	k = k.replaceAll("http://nwalsh.com/rdf/vCard#", "vCard_");
	k = k.replaceAll("http://www.opengis.net/gml/", "geo_");
	return k;
    }

	private String transfromEntities(Collection<GabotoEntity> entities) {
      // initialize level map
      for(GabotoEntity entity : entities){
        levelMap.put(entity, new Integer(1));
      }
	
      JSONStringer json = new JSONStringer();
		
      try {
        json.array();
        for(GabotoEntity entity : entities){
          transformEntity(entity, json, 1);
        }
        json.endArray();
      } catch (JSONException e) {
        throw new GabotoRuntimeException(e);
      }
      return json.toString();
    }

	private void transformEntity(GabotoEntity entity, JSONStringer json, int level) throws JSONException {
		// store basic knowledge
		
		// begin new object
		json.object();
		
		// add uri
		json.key("uri")
		    .value(entity.getUri());
		
		// add type
		json.key("type")
		    .value(entity.getType());
		
		if(levelMap.containsKey(entity)){
			int storedLevel = levelMap.get(entity).intValue();
			
			if(storedLevel < level){
				json.key("referenced").value(true);
				json.endObject();
				return;
			}
			
			// store level in map
			levelMap.put(entity, new Integer(level));
		}
		
		if(level > nesting){
			json.key("nestingLimitReached").value(true);
			json.endObject();
			return;
		}
			
		
		if((getCollectProperties() & COLLECT_DIRECT_PROPERTIES) == COLLECT_DIRECT_PROPERTIES){
			for(Entry<String, Object> entry : entity.getAllDirectProperties().entrySet()){
				processEntityEntry(entry, json, level);
			}
		}
		
		if((getCollectProperties() & COLLECT_INDIRECT_PROPERTIES) == COLLECT_INDIRECT_PROPERTIES){
			for(Entry<String, Object> entry : entity.getAllIndirectProperties().entrySet()){
				processEntityEntry(entry, json, level);
			}
		}
		
		if((getCollectProperties() & COLLECT_PASSIVE_PROPERTIES) == COLLECT_PASSIVE_PROPERTIES){
			Set<Entry<String, Object>> passiveProperties = entity.getAllPassiveProperties().entrySet();
			if(passiveProperties.size() > 0){
				json.key("passiveProperties");
				json.object();
				for(Entry<String, Object> entry : passiveProperties)
					processEntityEntry(entry, json, level);
				json.endObject();
			}
		}
		// end object
		json.endObject();
	}
	
	@SuppressWarnings("unchecked")
  private void processEntityEntry(Entry<String, Object> entry, JSONStringer json, int level) throws JSONException{
		if(entry.getValue() instanceof String){
		    json.key(simplifyKey(entry.getKey()));
			json.value(entry.getValue());
		} else if(entry.getValue() instanceof GabotoEntity) {
		    json.key(simplifyKey(entry.getKey()));
			transformEntity((GabotoEntity) entry.getValue(), json, level + 1);
		} else if(entry.getValue() instanceof Collection){
		    json.key(simplifyKey(entry.getKey()));
			json.array();
			for(GabotoEntity innerEntity : (Collection<GabotoEntity>) entry.getValue()){
				transformEntity(innerEntity, json, level + 1);
			}
			json.endArray();
		} else if(entry.getValue() instanceof GabotoBean){
          try { 
            json.key(simplifyKey(entry.getKey()));
            // beans should be put into the same level ..
            transformBean((GabotoBean)entry.getValue(), json, level);
           } catch (JSONException e) { 
             System.err.println("Already added " + simplifyKey(entry.getKey()));
           }
		} else if(entry.getValue() == null){
          try { 
            json.key(simplifyKey(entry.getKey()));
            json.value(null);
           } catch (JSONException e) { 
             System.err.println("Already added " + simplifyKey(entry.getKey()));
           }
		}
	}

	@SuppressWarnings("unchecked")
  private void transformBean(GabotoBean bean, JSONStringer json, int level) throws JSONException {
		if(level > nesting){
			json.object();
			json.key("nestingLimitReached").value(true);
			json.endObject();
			return;
		}
			
		
		// begin new object
		json.object();
		
		for(Entry<String, Object> entry : bean.getAllProperties().entrySet()){
			if(entry.getValue() instanceof String){
			    json.key(simplifyKey(entry.getKey()));
				json.value(entry.getValue());
			} else if(entry.getValue() instanceof GabotoEntity) {
			    json.key(simplifyKey(entry.getKey()));
				transformEntity((GabotoEntity) entry.getValue(), json, level + 1);
			} else if(entry.getValue() instanceof Collection){
			    json.key(simplifyKey(entry.getKey()));
				json.array();
				for(GabotoEntity innerEntity : (Collection<GabotoEntity>) entry.getValue()){
					transformEntity(innerEntity, json, level + 1);
				}
				json.endArray();
			} else if(entry.getValue() instanceof GabotoBean){
			    json.key(simplifyKey(entry.getKey()));
				transformBean((GabotoBean) entry.getValue(), json, level + 1);
			}
		}
		
		// end object
		json.endObject();
	}

	/**
	 * A binary or-ed value, defining which properties are to be added to the JSON output.
	 * 
	 * <p>
	 * The default is:
	 * COLLECT_DIRECT_PROPERTIES | COLLECT_INDIRECT_PROPERTIES | COLLECT_PASSIVE_PROPERTIES;
	 * </p>
	 * 
	 * @param collectProperties defines which properties are collected.
	 * @see JSONPoolTransformer#COLLECT_DIRECT_PROPERTIES
	 * @see JSONPoolTransformer#COLLECT_PASSIVE_PROPERTIES
	 * @see JSONPoolTransformer#COLLECT_INDIRECT_PROPERTIES
	 */
	public void setCollectProperties(int collectProperties) {
		this.collectProperties = collectProperties;
	}

	/**
	 * Returns which properties are to be collected.
	 * 
	 * @return which properties are to be collected.
	 */
	public int getCollectProperties() {
		return collectProperties;
	}

	/**
	 * @param nesting the nesting level 
	 */
	public void setNesting(int nesting) {
		if(nesting < 1 || nesting > MAX_NESTING)
			throw new IllegalArgumentException("Nesting has to be between 0 and " + MAX_NESTING);
		this.nesting = nesting;
	}

	/**
	 * @return the nesting
	 */
	public int getNesting() {
		return nesting;
	}
}
