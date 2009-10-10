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
package net.sf.gaboto.transformation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


import net.sf.gaboto.GabotoRuntimeException;
import net.sf.gaboto.node.GabotoBean;
import net.sf.gaboto.node.GabotoEntity;
import net.sf.gaboto.node.pool.EntityPool;

import org.json.JSONException;
import org.json.JSONStringer;

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
  private int collectProperties = COLLECT_DIRECT_PROPERTIES 
      | COLLECT_INDIRECT_PROPERTIES | COLLECT_PASSIVE_PROPERTIES;

  public String transform(EntityPool pool) {
    Collection<GabotoEntity> entities = pool.getEntities();
    // Initialize level map
    for (GabotoEntity entity : entities) {
      levelMap.put(entity, new Integer(1));
    }

    JSONStringer json = new JSONStringer();
    
    startArray(json);
    for (GabotoEntity entity : entities) {
      addEntity(json, entity, 1);
    }
    endArray(json);
    return  json.toString();
  }

  private void addEntity(JSONStringer json, GabotoEntity entity, int level) {
    //System.err.println("JSONPoolTransformer.addEntity:" + entity.getUri());
    // begin new object
    startObject(json);
    

    addKey(json, "uri");
    addValue(json, entity.getUri());

    addKey(json, "type");
    addValue(json, entity.getType());

    if (levelMap.containsKey(entity)) {
      int storedLevel = levelMap.get(entity).intValue();

      if (storedLevel < level) {
        addKey(json, "referenced");
        addValue(json, true);
        endObject(json);
        return;
      } 

      // store level in map
      levelMap.put(entity, new Integer(level));
    }

    if (level > nesting) {
      addKey(json, "nestingLimitReached");
      addValue(json, true);
      endObject(json);
      return;
    }
    
    //System.err.println("DIRECT PROPERTIES");

    if ((getCollectProperties() & COLLECT_DIRECT_PROPERTIES) == COLLECT_DIRECT_PROPERTIES) {
      for (Entry<String, Object> entry : entity.getAllDirectProperties().entrySet()) {
        addMember(json, entry.getKey(), entry.getValue(), level);
      }
    }

    //System.err.println("INDIRECT PROPERTIES");
    
    if ((getCollectProperties() & COLLECT_INDIRECT_PROPERTIES) == COLLECT_INDIRECT_PROPERTIES) {
      for (Entry<String, Object> entry : entity.getAllIndirectProperties().entrySet()) {
        // For Rooms, for example, it is possible for a property to exist in 
        // direct and indirect properties
        if (!entity.getAllDirectProperties().containsKey(entry.getKey()))
          addMember(json, entry.getKey(), entry.getValue(), level);
      }
    }

    //System.err.println("PASSIVE PROPERTIES");

    if ((getCollectProperties() & COLLECT_PASSIVE_PROPERTIES) == COLLECT_PASSIVE_PROPERTIES) {
      Set<Entry<String, Object>> passiveProperties = entity.getAllPassiveProperties().entrySet();
      if (passiveProperties.size() > 0) {
        boolean propertiesDefined = false;
        for (Entry<String, Object> entry : passiveProperties) {
          if (entry.getValue() != null)
            propertiesDefined = true;
        }        
        if (propertiesDefined) { 
          //System.err.println(passiveProperties.size());
          addKey(json, "passiveProperties");
          startObject(json);
          for (Entry<String, Object> entry : passiveProperties) {
            //System.err.println("passive entry" + entry.getKey() + "=" + entry.getValue());
            addMember(json, entry.getKey(), entry.getValue(), level);
          }
          endObject(json);
        }
      }
    }
    endObject(json);
  }

  HashMap<String, Object> contents = null;
  String indent = "";
  private JSONStringer startObject(JSONStringer jsonStringer) { 
    //System.err.println(indent + "Start Object");
    try {
      jsonStringer.object();
    } catch (JSONException e) {
      throw new GabotoRuntimeException(e);
    }
    contents = new HashMap<String, Object>();
    indent = indent + " ";
    return jsonStringer;
  }
  private JSONStringer endObject(JSONStringer jsonStringer) { 
    try {
      jsonStringer.endObject();
    } catch (JSONException e) {
      throw new GabotoRuntimeException(e);
    }
    indent = indent.substring(0, indent.length() -1);
    //System.err.println(indent + "End Object");
    return jsonStringer;
  }
  
  private JSONStringer startArray(JSONStringer jsonStringer)  { 
    try {
      jsonStringer.array();
    } catch (JSONException e) {
      throw new GabotoRuntimeException(e);
    }
    return jsonStringer;
  }
  private JSONStringer endArray(JSONStringer jsonStringer) { 
    try {
      jsonStringer.endArray();
    } catch (JSONException e) {
      throw new GabotoRuntimeException(e);
    }
    return jsonStringer;
  }

  private JSONStringer addKey(JSONStringer jsonStringer, String key) {
    //System.err.println(indent + "Adding key " + key);
    try {
      jsonStringer.key(key);
    } catch (JSONException e) {
      throw new GabotoRuntimeException(e);
    }
    return jsonStringer;
  }
  private JSONStringer addValue(JSONStringer jsonStringer, String value)  {
    //System.err.println(indent + "Adding value " + value);
    try {
      jsonStringer.value(value);
    } catch (JSONException e) {
      throw new GabotoRuntimeException(e);
    }
    return jsonStringer;
  }
  private JSONStringer addValue(JSONStringer jsonStringer, boolean value) {
    //System.err.println(indent + "Adding value " + value);
    try {
      jsonStringer.value(value);
    } catch (JSONException e) {
      throw new GabotoRuntimeException(e);
    }
    return jsonStringer;
  }
  private JSONStringer addValue(JSONStringer jsonStringer, Object value){
    //System.err.println(indent + "Adding value " + value);
    try {
      jsonStringer.value(value);
    } catch (JSONException e) {
      throw new GabotoRuntimeException(e);
    }
    return jsonStringer;
  }
  
  @SuppressWarnings("unchecked")
  private void addMember(JSONStringer jsonStringer, String memberName, Object memberValue,  int level) {
    if (memberValue == null)
      return; // No need to define null values
    String key = simplifyKey(memberName);
    if(contents.containsKey(key))
      throw new RuntimeException("Already in" + key);
    if (memberValue instanceof String) {
      addKey(jsonStringer, key);
      addValue(jsonStringer, memberValue);
    } else if (memberValue instanceof Integer) {
      addKey(jsonStringer, key);
      addValue(jsonStringer, memberValue);
    } else if (memberValue instanceof GabotoEntity) {
      addKey(jsonStringer, key);
      addEntity(jsonStringer, (GabotoEntity) memberValue, level + 1);
    } else if (memberValue instanceof Collection) {
      if (((Collection<GabotoEntity>) memberValue).size() == 0)
        return;
      addKey(jsonStringer, key);
      startArray(jsonStringer);
      for (GabotoEntity innerEntity : (Collection<GabotoEntity>) memberValue) {
        addEntity(jsonStringer, innerEntity, level + 1);
      }
      endArray(jsonStringer);
    } else if (memberValue instanceof GabotoBean) {
      try {
        addKey(jsonStringer, key);
      } catch (GabotoRuntimeException e) {
        // NOTE This is why we need our own JSONWriter
        // As the comma has already been written when the error is thrown
        // However it would be cooler if the problem was not caused
        throw new GabotoRuntimeException("Bean already added " + key + "(" + memberValue + ")", e);
        //return;
      }
      // beans should be put into the same level ..
      addBean(jsonStringer, (GabotoBean) memberValue, level);
    }  
      /*
       * If it is decided that null are needed after all 
      else  if (memberValue == null) {
      try {
        addKey(json, key);
        addValue(jsonStringer, null);
      } catch (JSONException e) {
        //System.err.println("Null value, already added " + key);
      }
      */    
    else
      throw new GabotoRuntimeException("Uncatered for condiditon." + 
          memberValue + "( Class " + memberValue.getClass() + ")");
  }
  @SuppressWarnings("unchecked")
  private void addBean(JSONStringer json, GabotoBean bean, int level) {
    if (level > nesting) {
      startObject(json);
      addKey(json,"nestingLimitReached");
      addValue(json, true);
      endObject(json);
      return;
    }

    startObject(json);

    for (Entry<String, Object> entry : bean.getAllProperties().entrySet()) {
      String  key = simplifyKey(entry.getKey()); 
      Object value = entry.getValue(); 
      if (value == null) {  
        // Do nothing, this is javascript
      } else if (value instanceof String) {
        addKey(json, key);
        addValue(json, value);
      } else if (value instanceof GabotoEntity) {
        addKey(json, key);
        addEntity(json, (GabotoEntity)value, level + 1);
      } else if (value instanceof Collection) {
        addKey(json, key);
        startArray(json);
        for (GabotoEntity innerEntity : (Collection<GabotoEntity>)value) {
          addEntity(json, innerEntity, level + 1);
        }
        endArray(json);
      } else if (value instanceof GabotoBean) {
        addKey(json, key);
        addBean(json, (GabotoBean)value, level + 1);
      } else
        throw new RuntimeException("Unanticipated type: " + key + "(" + value + ")" ); 
    }

    endObject(json);
  }

  /**
   * A binary or-ed value, defining which properties are to be added to the JSON
   * output.
   * 
   * <p>
   * The default is: COLLECT_DIRECT_PROPERTIES | COLLECT_INDIRECT_PROPERTIES |
   * COLLECT_PASSIVE_PROPERTIES;
   * </p>
   * 
   * @param collectProperties
   *          defines which properties are collected.
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
   * @param nesting
   *          the nesting level
   */
  public void setNesting(int nesting) {
    if (nesting < 1 || nesting > MAX_NESTING)
      throw new IllegalArgumentException("Nesting has to be between 0 and "
          + MAX_NESTING);
    this.nesting = nesting;
  }

  /**
   * @return the nesting
   */
  public int getNesting() {
    return nesting;
  }


  private String simplifyKey(String k) {
    String s = k;
    s = s.replaceAll("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#",
        "oxp_");
    s = s.replaceAll("http://ns.ox.ac.uk/namespace/gaboto/kml/2009/03/owl#",
        "gab_");
    s = s.replaceAll("http://purl.org/dc/elements/1.1/", "dc_");
    s = s.replaceAll("http://nwalsh.com/rdf/vCard#", "vCard_");
    s = s.replaceAll("http://www.opengis.net/gml/", "geo_");
    return s;
  }


}
