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

package org.oucs.gaboto;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.oucs.gaboto.beans.GabotoBean;
import org.oucs.gaboto.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Generates specialised classes from the Gaboto config file.
 * 
 * <p>
 * This class is very scripty and should be redone in an orderly fashion.
 * </p>
 * 
 * @author Arno Mittelbach
 *
 */
class GabotoClassGeneration {

	private File config = null;
	private File entityOutputDir;
	private File beanOutputDir;
	private File miscOutputDir;
	
	private String entityPackageName = "org.oucs.gaboto.entities";
	private String beanPackageName = "org.oucs.gaboto.beans";
	private String miscPackageName = "org.oucs.gaboto.util";
	
	private String entityClassNames = "";
	
	private Collection<String> entityNames = new HashSet<String>();
	private Collection<String> entityTypes = new HashSet<String>();
	private Map<String, String> entityClassLookup = new HashMap<String,String>();
	
	// type 2 uri
	private Map<String, String> entityTypeURILookup = new HashMap<String,String>();
	
	public final static int LITERAL_TYPE_STRING = 1;
	public final static int LITERAL_TYPE_INTEGER = 2;
	public final static int LITERAL_TYPE_FLOAT = 3;
	public final static int LITERAL_TYPE_DOUBLE = 4;
	public final static int LITERAL_TYPE_BOOLEAN = 5;
	
	
	public final static int SIMPLE_LITERAL_PROPERTY = 1;
	public final static int SIMPLE_URI_PROPERTY = 2;
	public final static int SIMPLE_COMPLEX_PROPERTY = 3;
	public final static int BAG_LITERAL_PROPERTY = 4;
	public final static int BAG_URI_PROPERTY = 5;
	public final static int BAG_COMPLEX_PROPERTY = 6;
	
	public final static String IMPORT_STMTS = 
									"import java.util.HashMap;\n" + 
									"import java.util.Map;\n" +
									"import java.util.Collection;\n" +
									"import java.util.HashSet;\n" +
									"import java.util.Set;\n" +
									"import java.util.List;\n" +
									"import java.util.ArrayList;\n" +
									
									"import java.lang.reflect.Method;\n" +
								
									
									"import org.oucs.gaboto.entities.utils.SimpleLiteralProperty;\n" +
									"import org.oucs.gaboto.entities.utils.SimpleURIProperty;\n" +
									"import org.oucs.gaboto.entities.utils.ComplexProperty;\n" +
									"import org.oucs.gaboto.entities.utils.BagURIProperty;\n" +
									"import org.oucs.gaboto.entities.utils.BagLiteralProperty;\n" +
									"import org.oucs.gaboto.entities.utils.BagComplexProperty;\n" +
									"import org.oucs.gaboto.entities.utils.IndirectProperty;\n" +
									"import org.oucs.gaboto.entities.utils.UnstoredProperty;\n" +
									"import org.oucs.gaboto.entities.utils.PassiveProperty;\n" +
									"import org.oucs.gaboto.entities.utils.StaticProperty;\n" +
									
									"import org.oucs.gaboto.vocabulary.*;\n" + 
									
									"import org.oucs.gaboto.entities.GabotoEntity;\n" +
									
									"import org.oucs.gaboto.entities.pool.GabotoEntityPool;\n" +
									"import org.oucs.gaboto.entities.pool.EntityExistsCallback;\n" +
									"import org.oucs.gaboto.entities.pool.PassiveEntitiesRequest;\n" +
									"import org.oucs.gaboto.model.GabotoSnapshot;\n" +
									
									"import com.hp.hpl.jena.rdf.model.Resource;\n" +
									"import com.hp.hpl.jena.rdf.model.Statement;\n" +
									"import com.hp.hpl.jena.rdf.model.StmtIterator;\n" + 
									"import com.hp.hpl.jena.rdf.model.Property;\n" + 
									"import com.hp.hpl.jena.rdf.model.Literal;\n" + 
									"import com.hp.hpl.jena.rdf.model.Bag;\n" +
									"import com.hp.hpl.jena.rdf.model.NodeIterator;\n" +
									"import com.hp.hpl.jena.rdf.model.RDFNode;\n" +
									
									"import com.hp.hpl.jena.ontology.OntClass;\n";
	
	
	public GabotoClassGeneration(File config, File entityOutputDir, File beanOutputDir, File miscOutputDir) {
		this.config = config;
		this.entityOutputDir = entityOutputDir;
		this.beanOutputDir = beanOutputDir;
		this.miscOutputDir = miscOutputDir;
	}


	public void run() throws ParserConfigurationException, SAXException, IOException {
		// load document
		Document doc = XMLUtils.readInputFileIntoJAXPDoc(config);
		
		// generate entities
		Element root = doc.getDocumentElement();
		NodeList children = root.getChildNodes();
		
		// load entity types
		for(int i = 0; i < children.getLength();i++){
			if(! (children.item(i) instanceof Element))
				continue;
			// cast
			Element el = (Element) children.item(i);
			
			if(el.getNodeName().equals("GabotoEntities")){
				NodeList entities = el.getChildNodes();

				// now generate the entities
				for (int j = 0; j < entities.getLength(); j++) {
					if(! (entities.item(j) instanceof Element))
						continue;
					Element entityEl = (Element) entities.item(j);
					
					loadEntityInformation(entityEl);
				}
			}
		}
		
		// generate classes
		for(int i = 0; i < children.getLength();i++){
			if(! (children.item(i) instanceof Element))
				continue;
			// cast
			Element el = (Element) children.item(i);
			
			if(el.getNodeName().equals("GabotoEntities")){
				NodeList entities = el.getChildNodes();

				// now generate the entities
				for (int j = 0; j < entities.getLength(); j++) {
					if(! (entities.item(j) instanceof Element))
						continue;
					Element entityEl = (Element) entities.item(j);
					generateEntity(entityEl, "GabotoEntity");
				}
			} else if(el.getNodeName().equals("GabotoBeans")){
				NodeList beans = el.getChildNodes();

				// now generate the beans
				for (int j = 0; j < beans.getLength(); j++) {
					if(! (beans.item(j) instanceof Element))
						continue;
					Element beanEl = (Element) beans.item(j);
					generateBean(beanEl, "GabotoBean");
				}
			}
		}
		
		// generate lookup class
		
		String lookupClass = "package " + miscPackageName + ";\n\n";
		lookupClass += IMPORT_STMTS + "\n\n";
		lookupClass += "@SuppressWarnings(\"unchecked\")\n";
	    lookupClass += "public class GabotoOntologyLookup{\n";
		
	    lookupClass += "  private static Map<String,String> entityClassLookupNames;\n";
	    lookupClass += "  private static Map<String,Class<? extends GabotoEntity>> entityClassLookupClass;\n";
	    lookupClass += "  private static Map<Class<? extends GabotoEntity>, String> classToURILookup;\n";
	    lookupClass += "  private static Collection<String> entityClassNames;\n";
		lookupClass += "  private static Set<String> entityTypes;\n\n";

		lookupClass += "  static{\n";
		lookupClass += "    entityClassLookupNames = new HashMap<String,String>();\n\n";
		for(Entry<String, String>entry : entityClassLookup.entrySet()){
			lookupClass += "    entityClassLookupNames.put(\"" + entry.getKey() + "\", \"" + entry.getValue() + "\");\n";
		}
		lookupClass += "  }\n\n";
		
		lookupClass += "  static{\n";
		lookupClass += "    entityClassLookupClass = new HashMap<String,Class<? extends GabotoEntity>>();\n\n";
		lookupClass += "    try {\n";
		for(Entry<String, String>entry : entityClassLookup.entrySet()){
			lookupClass += "      entityClassLookupClass.put(\"" + entry.getKey() + "\", (Class<?  extends GabotoEntity>) Class.forName(\"" +  entityPackageName + "." + entry.getValue() + "\"));\n";
		}
		lookupClass += "    } catch (ClassNotFoundException e) {\n";
		lookupClass += "      e.printStackTrace();\n";
		lookupClass += "    }\n";
		lookupClass += "  }\n\n";
		
		lookupClass += "  static{\n";
		lookupClass += "    classToURILookup = new HashMap<Class<? extends GabotoEntity>, String>();\n\n";
		lookupClass += "    try {\n";
		for(Entry<String, String>entry : entityClassLookup.entrySet()){
			lookupClass += "      classToURILookup.put((Class<?  extends GabotoEntity>) Class.forName(\"" +  entityPackageName + "." + entry.getValue() + "\"), \"" + entry.getKey() + "\");\n";
		}
		lookupClass += "    } catch (ClassNotFoundException e) {\n";
		lookupClass += "      e.printStackTrace();\n";
		lookupClass += "    }\n";
		lookupClass += "  }\n\n";

		lookupClass += "  static{\n";
		lookupClass += "    entityTypes = new HashSet<String>();\n\n";
		for(String type : entityTypes){
			lookupClass += "    entityTypes.add(\"" + type + "\");\n";
		}
		lookupClass += "  }\n\n";
		
		lookupClass += "  static{\n";
		lookupClass += "    entityClassNames = new HashSet<String>();\n\n";
		lookupClass += entityClassNames;
		lookupClass += "  }\n\n";
		
		lookupClass += "  public static Set<String> getRegisteredClassesAsURIs(){\n";
		lookupClass += "    return entityTypes;\n";
		lookupClass += "  }\n\n";
		
		lookupClass += "  public static Collection<String> getRegisteredEntityClassesAsClassNames(){\n";
		lookupClass += "    return entityClassNames;\n";
		lookupClass += "  }\n\n";
		
		lookupClass += "  public static Class<? extends GabotoEntity> getEntityClassFor(String typeURI){\n";
		lookupClass += "    return entityClassLookupClass.get(typeURI);\n";
		lookupClass += "  }\n\n";
		
		lookupClass += "  public static String getLocalName(String typeURI){\n";
		lookupClass += "    return entityClassLookupNames.get(typeURI);\n";
		lookupClass += "  }\n\n";
		
		lookupClass += "  public static String getTypeURIForEntityClass(Class<? extends GabotoEntity> clazz){\n";
		lookupClass += "    return classToURILookup.get(clazz);\n";
		lookupClass += "  }\n\n";
		
		lookupClass += "}\n";
		
		
		
		// write file
		try {
			File outputFile = new File(miscOutputDir.getAbsolutePath() + File.separator + "GabotoOntologyLookup.java");
			System.out.println("Write java class to: " + outputFile.getAbsolutePath());
			BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
			out.write(lookupClass);
			out.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	
	private void loadEntityInformation(Element entityEl) {
		entityNames.add(entityEl.getAttribute("name"));
		entityTypeURILookup.put(entityEl.getAttribute("name"), entityEl.getAttribute("type"));

		// recursion
		NodeList children = entityEl.getChildNodes();
		for(int i = 0; i < children.getLength(); i++){
			if(! (children.item(i) instanceof Element))
				continue;
			
		    if(children.item(i).getNodeName().equals("GabotoEntities")){
					NodeList entities = children.item(i).getChildNodes();
					for(int j = 0; j < entities.getLength(); j++){
						if(! (entities.item(j) instanceof Element))
							continue;
						
						if(entities.item(j).getNodeName().equals("GabotoEntity")){
							loadEntityInformation((Element) entities.item(j));
						}
					}
				} 
		}
	}


	private void generateBean(Element beanEl, String extendsDef) {
		if(! beanEl.getNodeName().equals("GabotoBean"))
			return;
		
		String importDefinitions = IMPORT_STMTS;
		
		// get name
		String name = beanEl.getAttribute("name");
		System.out.println("generate bean for file: " + name);
		
		// type definition
		String beanType = beanEl.getAttribute("type");
		String typeDef = "  @Override\n";
		typeDef += "  public String getType(){\n";
		typeDef += "    return \"" + beanType + "\";\n";
		typeDef += "  }\n";
		
		// loadEntityMethod
		boolean bBeanHasProperty = false;
		String loadBeanMethod = "  public void loadFromResource(Resource res, GabotoSnapshot snapshot, GabotoEntityPool pool) {\n";
		loadBeanMethod += "    super.loadFromResource(res, snapshot, pool);\n";
		loadBeanMethod += "    Statement stmt;\n\n";
		
		
		// custom methods
		String customMethods = "";
		
		// get properties
		String propertyDefinitions = "";
		String methodDefinitions = "";
		NodeList children = beanEl.getChildNodes();
		for(int i = 0; i < children.getLength(); i++){
			if(! (children.item(i) instanceof Element))
				continue;
			if(children.item(i).getNodeName().equals("properties")){
				NodeList properties = children.item(i).getChildNodes();
				for(int j = 0; j < properties.getLength(); j++){
					if(! (properties.item(j) instanceof Element))
						continue;
					
					if(properties.item(j).getNodeName().equals("property")){
						bBeanHasProperty = true;
						
						// cast property
						Element property = (Element) properties.item(j);
						
						String[] processedProperty = processProperty(property, null, null, null, null);
						importDefinitions += processedProperty[0];
						propertyDefinitions += processedProperty[1];
						methodDefinitions += processedProperty[2];
						loadBeanMethod += processedProperty[3];
					}
				}
			} else if(children.item(i).getNodeName().equals("customMethods")){
				NodeList methods = children.item(i).getChildNodes();
				for(int j = 0; j < methods.getLength(); j++){
					if(! (methods.item(j) instanceof Element))
						continue;
					
					if(methods.item(j).getNodeName().equals("method")){
						customMethods += ((Element)methods.item(j)).getNodeValue();
					}
				}		
			}
		}
		
		// load entity method
		loadBeanMethod += "  }\n";
		
		
		String clazz = "package " + beanPackageName + ";\n\n";
		clazz += importDefinitions + "\n\n";
		clazz += "public class ";
		clazz += name + " extends GabotoBean {\n";
		clazz += propertyDefinitions;
		clazz += typeDef;
		clazz += methodDefinitions + "\n\n";
		clazz += customMethods + "\n\n";
		if(bBeanHasProperty)
			clazz += loadBeanMethod;
		clazz += "}";
		
		// write file
		try {
			File outputFile = new File(beanOutputDir.getAbsolutePath() + File.separator + name + ".java");
			System.out.println("Write java class to: " + outputFile.getAbsolutePath());
			BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
			out.write(clazz);
			out.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	

	private void generateEntity(Element entityEl, String extendsDef) {
		if(! entityEl.getNodeName().equals("GabotoEntity"))
			return;
		
		// import
		String importDefinitions = IMPORT_STMTS;
		importDefinitions += "import org.oucs.gaboto.entities." + extendsDef + ";\n";
		
		// get name
		String entityName = entityEl.getAttribute("name");
		System.out.println("generate entity for file: " + entityName);
		
		// class comment
		String classComment = "/**\n";
		
		// type definition
		String entityType = entityEl.getAttribute("type");
		String typeDef = "  @Override\n";
		typeDef += "  public String getType(){\n";
		typeDef += "    return \"" + entityType + "\";\n";
		typeDef += "  }\n";
		entityTypes.add(entityType);
		
		// add line to classname collection
		entityClassNames += "    entityClassNames.add(\"" + entityName + "\");\n";
		
		// add line to lookup
		entityClassLookup.put(entityType, entityName);
		
		// loadEntityMethod
		boolean bEntityHasProperty = false;
		String loadEntityMethod = "  public void loadFromSnapshot(Resource res, GabotoSnapshot snapshot, GabotoEntityPool pool) {\n";
		loadEntityMethod += "    super.loadFromSnapshot(res, snapshot, pool);\n";
		loadEntityMethod += "    Statement stmt;\n\n";
		
		// passive entity requests
		boolean bEntityHasPassiveProperty = false;
		String passiveEntityRequests = "  public Collection<PassiveEntitiesRequest> getPassiveEntitiesRequest(){\n";
		passiveEntityRequests += "    Collection<PassiveEntitiesRequest> requests = super.getPassiveEntitiesRequest();\n";
		passiveEntityRequests += "    if(requests == null)\n";
		passiveEntityRequests += "      requests = new HashSet<PassiveEntitiesRequest>();\n";
		
		
		// custom methods
		String customMethods = "";
		
		// indirect methods
		Map<String,List<String>> indirectMethodLookup = new HashMap<String, List<String>>();
		Map<String,String> indirectMethods = new HashMap<String, String>();
		
		// unstored methods
		Map<String,String> unstoredMethods = new HashMap<String, String>();
		
		// get properties
		String propertyDefinitions = "";
		String methodDefinitions = "";
		NodeList children = entityEl.getChildNodes();
		for(int i = 0; i < children.getLength(); i++){
			if(! (children.item(i) instanceof Element))
				continue;
			if(children.item(i).getNodeName().equals("properties")){
				NodeList properties = children.item(i).getChildNodes();
				for(int j = 0; j < properties.getLength(); j++){
					if(! (properties.item(j) instanceof Element))
						continue;
					
					if(properties.item(j).getNodeName().equals("property")){
						bEntityHasProperty = true;
						
						// cast property
						Element property = (Element) properties.item(j);
						
						String[] processedProperty = processProperty(property, unstoredMethods, indirectMethods, indirectMethodLookup, entityName);
						importDefinitions += processedProperty[0];
						propertyDefinitions += processedProperty[1];
						methodDefinitions += processedProperty[2];
						loadEntityMethod += processedProperty[3];	
					} else if(properties.item(j).getNodeName().equals("passiveProperty")){
						bEntityHasPassiveProperty = true;
						
						// cast property
						Element property = (Element) properties.item(j);
						
						String[] processedProperty = processPassiveProperty(property, indirectMethods, indirectMethodLookup, entityName);
						importDefinitions += processedProperty[0];
						propertyDefinitions += processedProperty[1];
						methodDefinitions += processedProperty[2];
						passiveEntityRequests += processedProperty[3];
					}
				}
			} else if(children.item(i).getNodeName().equals("GabotoEntities")){
				NodeList entities = children.item(i).getChildNodes();
				for(int j = 0; j < entities.getLength(); j++){
					if(! (entities.item(j) instanceof Element))
						continue;
					
					if(entities.item(j).getNodeName().equals("GabotoEntity")){
						generateEntity((Element) entities.item(j), entityName);
					}
				}
			} else if(children.item(i).getNodeName().equals("customMethods")){
				NodeList methods = children.item(i).getChildNodes();
				for(int j = 0; j < methods.getLength(); j++){
					if(! (methods.item(j) instanceof Element))
						continue;
					
					if(methods.item(j).getNodeName().equals("method")){
						customMethods += ((Element)methods.item(j)).getNodeValue();
					}
				}		
			}
		}
		
		// indirect properties
		String indirectPropertyMethods = "";
		for(String method : indirectMethods.values())
			indirectPropertyMethods += method;
		
		// unstored properties
		String unstoredPropertyMethods = "";
		for(String method : unstoredMethods.values())
			unstoredPropertyMethods += method;
		
		// indirect property position lookup
		String indirectPropertyLookupTable = "  private static Map<String, List<Method>> indirectPropertyLookupTable;\n";
		indirectPropertyLookupTable += "  static{\n";
		indirectPropertyLookupTable += "    indirectPropertyLookupTable = new HashMap<String, List<Method>>();\n";
		indirectPropertyLookupTable += "    List<Method> list;\n\n";
		
		indirectPropertyLookupTable += "    try{\n";
		for(Entry<String, List<String>> entry : indirectMethodLookup.entrySet()){
			indirectPropertyLookupTable += "      list = new ArrayList<Method>();\n";
			for(String property : entry.getValue()){
				if(property != null )
					indirectPropertyLookupTable += "      list.add(" + property + ");\n";
			}
			indirectPropertyLookupTable += "      indirectPropertyLookupTable.put(\"" + entry.getKey() + "\", list);\n\n";
		}
		indirectPropertyLookupTable += "    } catch (Exception e) {\n";
		indirectPropertyLookupTable += "      e.printStackTrace();\n";
		indirectPropertyLookupTable += "    }\n";
		indirectPropertyLookupTable += "  }\n\n";

		// indirect property method 
		String indirectPropertyLoookupMethod = "  protected List<Method> getIndirectMethodsForProperty(String propertyURI){\n";
		indirectPropertyLoookupMethod += "    List<Method> list = super.getIndirectMethodsForProperty(propertyURI);\n";
		indirectPropertyLoookupMethod += "    if(list == null)\n";
		indirectPropertyLoookupMethod += "      return indirectPropertyLookupTable.get(propertyURI);\n";
		indirectPropertyLoookupMethod += "    \n";
		indirectPropertyLoookupMethod += "    else{\n";
		indirectPropertyLoookupMethod += "      List<Method> tmp = indirectPropertyLookupTable.get(propertyURI);\n";
		indirectPropertyLoookupMethod += "      if(tmp != null)\n";
		indirectPropertyLoookupMethod += "        list.addAll(tmp);\n";
		indirectPropertyLoookupMethod += "    }\n";
		indirectPropertyLoookupMethod += "    return list;\n";
		indirectPropertyLoookupMethod += "  }\n\n";
		
	

		
		// close class comment
    classComment += " * Gaboto generated class.\n";
    classComment += " * See " + this.getClass().getCanonicalName() + ".\n";
		classComment += " */\n";
		
		// load entity method
		loadEntityMethod += "  }\n";
		
		// passive entity request
		passiveEntityRequests += "    return requests;\n";
		passiveEntityRequests += "  }\n";
		
		// add some newlines
		propertyDefinitions += "\n\n";
		methodDefinitions += "\n\n";
		typeDef += "\n";
		
		// abstract
		boolean abstractClass = entityEl.hasAttribute("abstract") ? entityEl.getAttribute("abstract").equals("true") : false;
		
		// build it all together
		String clazz = "package " + entityPackageName + ";\n\n";
		clazz += importDefinitions + "\n\n";
		clazz += classComment;
		if(abstractClass)
			clazz += "abstract ";
		clazz += "public class ";
		clazz += entityName + " extends " + extendsDef + " {\n";
		clazz += propertyDefinitions;
		clazz += indirectPropertyLookupTable;
		clazz += typeDef;
		clazz += methodDefinitions + "\n\n";
		clazz += indirectPropertyMethods;
		clazz += unstoredPropertyMethods;
		clazz += customMethods + "\n\n";
		if(bEntityHasPassiveProperty)
			clazz += passiveEntityRequests + "\n\n";
		if(bEntityHasProperty)
			clazz += loadEntityMethod;
		clazz += indirectPropertyLoookupMethod;
		
		clazz += "}";
		
		// write file
		try {
			File outputFile = new File(entityOutputDir.getAbsolutePath() + File.separator + entityName + ".java");
			System.out.println("Write java class to: " + outputFile.getAbsolutePath());
			BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
			out.write(clazz);
			out.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}




	/**
	 * 
	 * @param property
	 * @param indirectMethodLookup 
	 * @param entityName 
	 * @return {importDefinitions, propertyDefinitionss, methodDefinitions}
	 */
	private String[] processProperty(Element property, Map<String, String> unstoredMethods, Map<String, String> indirectMethods, Map<String, List<String>> indirectMethodLookup, String entityName) {
		String  importDefinitions = "",
				propertyDefinitionss = "",
				methodDefinitions = "",
				loadEntitySnippet = "";
		
		String propName = property.getAttribute("name").substring(0,1).toLowerCase() + property.getAttribute("name").substring(1);
		String propNameUCFirst = propName.substring(0,1).toUpperCase() + propName.substring(1);
		String propType = property.getAttribute("type");
		String collection = property.getAttribute("collection").toLowerCase();
		String uri = property.getAttribute("uri");
		
		// real prop type 
		String realPropTypeInterface = propType;
		String realPropTypeImpl = propType;
		if(collection.equals("bag")){
			realPropTypeInterface = "Collection<" + propType + ">";
			realPropTypeImpl = "HashSet<" + propType + ">";
		}
		
		String getMethodName = "get" + propNameUCFirst;
		
		// annotations
		String propertyAnnotation = getPropertyAnnotation(propType, uri, collection);
		String indirectAnnotation = getIndirectAnnotation(property, indirectMethods, indirectMethodLookup, entityName, getMethodName);
		String unstoredAnnotation = getUnstoredAnnotation(property, unstoredMethods, realPropTypeImpl);
		
		// property definition
		propertyDefinitionss += "  private " + realPropTypeInterface + " " + propName + ";\n";
		
		// add additional imports
		importDefinitions += getBeanImport(propType);
		
		
		// get method
		if(! unstoredAnnotation.equals(""))
			methodDefinitions += "  " + unstoredAnnotation + "\n";
		if(! indirectAnnotation.equals(""))
			methodDefinitions += "  " + indirectAnnotation + "\n";
		methodDefinitions += "  " + propertyAnnotation + "\n";
		switch(getPropertyAnnotationType(property)){
		case BAG_URI_PROPERTY:
		case SIMPLE_URI_PROPERTY:
			methodDefinitions += getGetMethodForDirectReference(realPropTypeInterface, getMethodName, propName);
			break;
		default:
			methodDefinitions += getGetMethod(realPropTypeInterface, getMethodName, propName);
			break;
		}
		
		
		// set method
		String setMethodName = "set" + propNameUCFirst;
		methodDefinitions += "  " + propertyAnnotation + "\n";
		switch(getPropertyAnnotationType(property)){
		case SIMPLE_URI_PROPERTY:
			methodDefinitions += getSetMethodForSimpleURIProperty("public", setMethodName, realPropTypeInterface, propName, propName);
			break;
		case BAG_URI_PROPERTY:
			methodDefinitions += getSetMethodForBagURIProperty("public", setMethodName, realPropTypeInterface, propName, propName);
			break;
		default: 
			methodDefinitions += getSetMethod("public", setMethodName, realPropTypeInterface, propName, propName);
			break;
		}
		
		
		// add method
		String addMethodName = "add";
		if(collection.equals("bag")){
			String parameterName = "";
			if(propNameUCFirst.endsWith("s")){
				addMethodName += propNameUCFirst.substring(0, propNameUCFirst.length() - 1);
				parameterName = propName.substring(0, propName.length() - 1);
			} else {
				addMethodName += propNameUCFirst;
				parameterName = propName;
			}
					
			switch(getPropertyAnnotationType(property)){
			case BAG_URI_PROPERTY:
				methodDefinitions += getAddMethodForBagURI("public", addMethodName, propType, realPropTypeInterface, realPropTypeImpl, parameterName, propName);
				break;
			default:
				methodDefinitions += getAddMethod("public", addMethodName, propType, realPropTypeInterface, realPropTypeImpl, parameterName, propName);
				break;
			}
			
		}
		
		// load entity snippet
		loadEntitySnippet = getLoadEntitySnippet(property, uri, propType, realPropTypeInterface, realPropTypeImpl, propName, setMethodName, addMethodName);
		
		
		return new String[]{importDefinitions, propertyDefinitionss, methodDefinitions, loadEntitySnippet};
	}
	

	private String[] processPassiveProperty(Element property, Map<String, String> indirectMethods, Map<String, List<String>> indirectMethodLookup, String entityName) {
		String  importDefinitions = "",
				propertyDefinitionss = "",
				methodDefinitions = "",
				passiveEntityRequests = "";
		
		String relationshipType = property.getAttribute("relationshipType");
		String propType = property.getAttribute("type");
		String uri = property.getAttribute("uri");
		String propName = property.getAttribute("name");
		String propNameUCFirst = propName.substring(0,1).toUpperCase() + propName.substring(1);
		
		// build annotation
		String annotation = "  @PassiveProperty(\n" +
							"    uri = \"" + uri + "\",\n" +
							"    entity = \"" + propType + "\"\n" +
							"  )\n";
		
		String getMethodName = "",
			   setMethodName = "",
			   addMethodName = "";
		
		// what relationship type do we have
		if(relationshipType.equals("1:N") ||
		   relationshipType.equals("N:M")){
			// we can have N propTypes
			String realPropTypeInterface = "Collection<" + propType + ">";
			String realPropTypeImpl = "HashSet<" + propType + ">";

			// add member property
			propertyDefinitionss += "  private " + realPropTypeInterface + " " + propName + ";\n"; 
			
			// generate get/set/add method
			
			// get method
			getMethodName = "get" + propNameUCFirst;
			
			// indirect annotation
			String indirectAnnotation = getIndirectAnnotation(property, indirectMethods, indirectMethodLookup, entityName, getMethodName);
			
			if(!"".equals(indirectAnnotation))
				methodDefinitions += "  " + indirectAnnotation + "\n";
			methodDefinitions += annotation;
			methodDefinitions += "  public " + realPropTypeInterface + " " + getMethodName + "(){\n";
			methodDefinitions += "    if(! isPassiveEntitiesLoaded() )\n";
			methodDefinitions += "      loadPassiveEntities();\n";
			methodDefinitions += "    return this." + propName + ";\n"; 
			methodDefinitions += "  }\n\n";
			
			
			// set method
			setMethodName = "set" + propNameUCFirst;
			methodDefinitions += annotation;
			methodDefinitions += getSetMethod("private", setMethodName, realPropTypeInterface, propName, propName);
			
			// add method
			addMethodName = "add";
			String parameterName = "";
			if(propNameUCFirst.endsWith("s")){
				addMethodName += propNameUCFirst.substring(0, propNameUCFirst.length() - 1);
				parameterName = propName.substring(0, propName.length() - 1);
			} else {
				addMethodName += propNameUCFirst;
				parameterName = propName;
			}
			methodDefinitions += getAddMethod("private", addMethodName, propType, realPropTypeInterface, realPropTypeImpl, parameterName, propName);
			
		} else {
			throw new RuntimeException("Illegal relationship type: " + relationshipType);
		}
		
		// requests
		if(relationshipType.equals("1:N")){
			// request
			passiveEntityRequests += "    requests.add(new PassiveEntitiesRequest(){\n";
			passiveEntityRequests += "      public String getType() {\n";
			passiveEntityRequests += "        return \"" + entityTypeURILookup.get(propType) + "\";\n";
			passiveEntityRequests += "      }\n\n";
			passiveEntityRequests += "      public String getUri() {\n";
			passiveEntityRequests += "        return \"" + uri + "\";\n";
			passiveEntityRequests += "      }\n\n";
			passiveEntityRequests += "      public int getCollectionType() {\n";
			passiveEntityRequests += "        return GabotoEntityPool.PASSIVE_PROPERTY_COLLECTION_TYPE_NONE;\n";
			passiveEntityRequests += "      }\n\n";
			passiveEntityRequests += "      public void passiveEntityLoaded(GabotoEntity entity) {\n";
			passiveEntityRequests += "        " + addMethodName + "((" + propType + ")entity);\n";
			passiveEntityRequests += "      }\n";
			passiveEntityRequests += "    });\n";
		} else if(relationshipType.equals("N:M")){
			// request
			passiveEntityRequests += "    requests.add(new PassiveEntitiesRequest(){\n";
			passiveEntityRequests += "      public String getType() {\n";
			passiveEntityRequests += "        return \"" + entityTypeURILookup.get(propType) + "\";\n";
			passiveEntityRequests += "      }\n\n";
			passiveEntityRequests += "      public String getUri() {\n";
			passiveEntityRequests += "        return \"" + uri + "\";\n";
			passiveEntityRequests += "      }\n\n";
			passiveEntityRequests += "      public int getCollectionType() {\n";
			passiveEntityRequests += "        return GabotoEntityPool.PASSIVE_PROPERTY_COLLECTION_TYPE_BAG;\n";
			passiveEntityRequests += "      }\n\n";
			passiveEntityRequests += "      public void passiveEntityLoaded(GabotoEntity entity) {\n";
			passiveEntityRequests += "        " + addMethodName + "((" + propType + ")entity);\n";
			passiveEntityRequests += "      }\n";
			passiveEntityRequests += "    });\n";
		}
		
		return new String[]{importDefinitions, propertyDefinitionss, methodDefinitions, passiveEntityRequests};
	}
	
	private String getGetMethod(String returnType, String methodName, String memberName){
		String methodDefinition = "";
		
		methodDefinition += "  public " + returnType + " " + methodName + "(){\n";
		methodDefinition += "    return this." + memberName + ";\n"; 
		methodDefinition += "  }\n\n";

		return methodDefinition;
	}
	
	private String getGetMethodForDirectReference(String returnType, String methodName, String memberName){
		String methodDefinition = "";
		
		methodDefinition += "  public " + returnType + " " + methodName + "(){\n";
		methodDefinition += "    if(! this.isDirectReferencesResolved())\n";
		methodDefinition += "      this.resolveDirectReferences();\n";
		methodDefinition += "    return this." + memberName + ";\n"; 
		methodDefinition += "  }\n\n";

		return methodDefinition;
	}

	
	private String getSetMethod(String visibility, String methodName, String parameterType, String parameterName, String memberName){
		String methodDefinition = "";
		
		if(visibility.equals("package"))
			visibility = "";
		
		methodDefinition += "  " + visibility + " void " + methodName + "(" + parameterType + " " + parameterName + "){\n";
		methodDefinition += "    this." + memberName + " = " + parameterName + ";\n"; 
		methodDefinition += "  }\n\n";
		
		return methodDefinition;
	}
	
	private String getSetMethodForSimpleURIProperty(String visibility, String methodName, String parameterType, String parameterName, String memberName){
		String methodDefinition = "";
		
		if(visibility.equals("package"))
			visibility = "";
		
		methodDefinition += "  " + visibility + " void " + methodName + "(" + parameterType + " " + parameterName + "){\n";
		methodDefinition += "    if( " + parameterName + " != null )\n";
		methodDefinition += "      this.removeMissingReference( " + parameterName + ".getUri() );\n";
		methodDefinition += "    this." + memberName + " = " + parameterName + ";\n"; 
		methodDefinition += "  }\n\n";
		
		return methodDefinition;
	}
	
	private String getSetMethodForBagURIProperty(String visibility, String methodName, String parameterType, String parameterName, String memberName){
		String methodDefinition = "";
		
		if(visibility.equals("package"))
			visibility = "";
		
		methodDefinition += "  " + visibility + " void " + methodName + "(" + parameterType + " " + parameterName + "){\n";
		methodDefinition += "    if( " + parameterName + " != null ){\n";
		methodDefinition += "      for( GabotoEntity _entity : " + parameterName + ")\n";
		methodDefinition += "        this.removeMissingReference( _entity.getUri() );\n";
		methodDefinition += "    }\n\n";
		methodDefinition += "    this." + memberName + " = " + parameterName + ";\n"; 
		methodDefinition += "  }\n\n";
		
		return methodDefinition;
	}

	private String getAddMethod(String visibility, String methodName, String propType, String realPropTypeInterface, String realPropTypeImpl, String parameterName, String memberName){
		String methodDefinition = "";
		
		if(visibility.equals("package"))
			visibility = "";
		
		methodDefinition += "  " + visibility + " void " + methodName + "(" + propType + " " + parameterName + "){\n";
		methodDefinition += "    if(this." + memberName + " == null)\n";
		methodDefinition += "      this." + memberName + " = new " + realPropTypeImpl + "();\n";
		methodDefinition += "    this." + memberName + ".add(" + parameterName + ");\n";
		methodDefinition += "  }\n\n";
		
		return methodDefinition;
	}
	
	private String getAddMethodForBagURI(String visibility, String methodName, String propType, String realPropTypeInterface, String realPropTypeImpl, String parameterName, String memberName){
		String methodDefinition = "";
		
		if(visibility.equals("package"))
			visibility = "";
		
		methodDefinition += "  " + visibility + " void " + methodName + "(" + propType + " " + parameterName + "){\n";
		methodDefinition += "    if( " + parameterName + " != null )\n";
		methodDefinition += "      this.removeMissingReference( " + parameterName + ".getUri() );\n";
		methodDefinition += "    if(this." + memberName + " == null )\n";
		methodDefinition += "      this." + memberName + " = new " + realPropTypeImpl + "();\n";
		methodDefinition += "    this." + memberName + ".add(" + parameterName + ");\n";
		methodDefinition += "  }\n\n";
		
		return methodDefinition;
	}
	
	private String getLoadEntitySnippet(Element property, String uri, String propType, String realPropTypeInterface, String realPropTypeImpl, String propertyName, String setMethodName, String addMethodName){
		String loadEntity = "";
		
		switch(getPropertyAnnotationType(property)){
		case SIMPLE_URI_PROPERTY:
			loadEntity += "    stmt = res.getProperty(snapshot.getProperty(\"" + uri + "\"));\n";
			loadEntity += "    if(stmt != null && stmt.getObject().isResource()){\n";
			loadEntity += "      Resource missingReference = (Resource)stmt.getObject();\n";
			
			loadEntity += "      EntityExistsCallback callback = new EntityExistsCallback(){\n";
			loadEntity += "        public void entityExists(GabotoEntityPool pool, GabotoEntity entity) {\n";
			loadEntity += "          " + setMethodName + "((" + realPropTypeInterface + ")entity);\n";
			loadEntity += "        }\n";
			loadEntity += "      };\n";
			
			loadEntity += "      this.addMissingReference(missingReference, callback);\n";
			loadEntity += "    }\n";
			break;
		case SIMPLE_LITERAL_PROPERTY:
			loadEntity += "    stmt = res.getProperty(snapshot.getProperty(\"" + uri + "\"));\n";
			loadEntity += "    if(stmt != null && stmt.getObject().isLiteral())\n";
			loadEntity += "      this." + setMethodName + "(((Literal)stmt.getObject())." + getLiteralGetMethod(property) + ");\n";
			break;
		case SIMPLE_COMPLEX_PROPERTY:
			loadEntity += "    stmt = res.getProperty(snapshot.getProperty(\"" + uri + "\"));\n";
			loadEntity += "    if(stmt != null && stmt.getObject().isAnon()){\n";
			loadEntity += "      " + realPropTypeInterface + " " + propertyName + " = new " + realPropTypeInterface + "();\n";
			loadEntity += "      " + propertyName + ".loadFromResource((Resource)stmt.getObject(), snapshot, pool);\n";
			loadEntity += "      " + setMethodName + "(" + propertyName + ");\n";
			loadEntity += "    }\n";
			break;
		case BAG_URI_PROPERTY:
			loadEntity += "    stmt = res.getProperty(snapshot.getProperty(\"" + uri + "\"));\n";
			loadEntity += "    if(stmt != null && stmt.getObject().isResource() && null != stmt.getBag()){\n";
			loadEntity += "      Bag bag = stmt.getBag();\n";
			loadEntity += "      NodeIterator nodeIt = bag.iterator();\n";
			loadEntity += "      while(nodeIt.hasNext()){\n";
			loadEntity += "        RDFNode node = nodeIt.nextNode();\n";
			loadEntity += "        if(! node.isResource())\n";
			loadEntity += "          throw new IllegalArgumentException(\"node should be a resource\");\n\n";
			
			loadEntity += "        Resource missingReference = (Resource)node;\n";
			
			loadEntity += "        EntityExistsCallback callback = new EntityExistsCallback(){\n";
			loadEntity += "          public void entityExists(GabotoEntityPool pool, GabotoEntity entity) {\n";
			loadEntity += "            " + addMethodName + "((" + propType + ") entity);\n";
			loadEntity += "          }\n";
			loadEntity += "        };\n";
			
			loadEntity += "        this.addMissingReference(missingReference, callback);\n";
			loadEntity += "      }\n";
			loadEntity += "    }\n";
			break;
		case BAG_LITERAL_PROPERTY:
			loadEntity += "    stmt = res.getProperty(snapshot.getProperty(\"" + uri + "\"));\n";
			loadEntity += "    if(stmt != null && stmt.getObject().isResource() && null != stmt.getBag()){\n";
			loadEntity += "      Bag bag = stmt.getBag();\n";
			loadEntity += "      NodeIterator nodeIt = bag.iterator();\n";
			loadEntity += "      while(nodeIt.hasNext()){\n";
			loadEntity += "        RDFNode node = nodeIt.nextNode();\n";
			loadEntity += "        if(! node.isLiteral())\n";
			loadEntity += "          throw new IllegalArgumentException(\"node should be a literal\");\n\n";
			loadEntity += "        " + addMethodName + "(((Literal)node)." + getLiteralGetMethod(property) + ");\n";
			loadEntity += "      }\n\n";
			loadEntity += "    }\n";
			break;
		case BAG_COMPLEX_PROPERTY:
			loadEntity += "    stmt = res.getProperty(snapshot.getProperty(\"" + uri + "\"));\n";
			loadEntity += "    if(stmt != null  && stmt.getObject().isResource() && null != stmt.getBag()){\n";
			loadEntity += "      Bag bag = stmt.getBag();\n";
			loadEntity += "      NodeIterator nodeIt = bag.iterator();\n";
			loadEntity += "      while(nodeIt.hasNext()){\n";
			loadEntity += "        RDFNode node = nodeIt.nextNode();\n";
			loadEntity += "        if(! node.isAnon())\n";
			loadEntity += "          throw new IllegalArgumentException(\"node should be a blank node\");\n\n";
			loadEntity += "        " + realPropTypeInterface + " " + propertyName + " = new " + realPropTypeInterface + "();\n";
			loadEntity += "        " + propertyName + ".loadFromResource((Resource)node, snapshot, pool);\n";
			loadEntity += "            " + addMethodName + "(" + propertyName + ");\n";
			loadEntity += "      }\n\n";
			loadEntity += "    }\n";
			break;
		}
		
		
		return loadEntity + "\n";
	}

	private String getBeanImport(String propType){
		try {
			String beansPackage = GabotoBean.class.getPackage().getName();
			Class<?> clazz = Class.forName(beansPackage + "." + propType);
			if(clazz.newInstance() instanceof GabotoBean)
				return "import " + beansPackage + "." + propType + ";\n";
		} catch (ClassNotFoundException e) {
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return "";
	}


	private String getIndirectAnnotation(Element property, Map<String, String> indirectMethods, Map<String, List<String>> indirectMethodLookup, String entityName, String getMethodName) {
		String anno = "";
		NodeList nodeList = property.getElementsByTagName("indirectProperty");
		boolean bFirst = true;
		for(int i = 0; i < nodeList.getLength(); i++){
			Element propEl = (Element) nodeList.item(i);
			
			String uri = propEl.getAttribute("uri");
			
			if(bFirst){
				anno = "\"" + uri + "\"";
				bFirst = false;
			} else 
				anno += ",\"" + uri + "\"";

			// store position
			if(null != indirectMethodLookup){
				int position = propEl.hasAttribute("n") ? Integer.valueOf(propEl.getAttribute("n")) - 1 : 0;
				if(! indirectMethodLookup.containsKey(uri))
					indirectMethodLookup.put(uri, new ArrayList<String>());
				
				List<String> list = indirectMethodLookup.get(uri);
				if(list.size() <= position){
					for(int j = list.size(); j < position; j++)
						list.add(null);
					list.add(entityName + ".class.getMethod(\"" + getMethodName + "\", (Class<?>[])null)");
				} else {
					list.add(position, entityName + ".class.getMethod(\"" + getMethodName + "\", (Class<?>[])null)");
				}
			}
			
			// indirect methods
			if(! indirectMethods.containsKey(uri) && propEl.hasAttribute("name")){
				String propName = propEl.getAttribute("name").substring(0,1).toLowerCase() + propEl.getAttribute("name").substring(1);
				String propNameUCFirst = propName.substring(0,1).toUpperCase() + propName.substring(1);
				
				String method = "  public Object get" + propNameUCFirst + "(){\n";
				method += "    return this.getPropertyValue(\"" + uri + "\", false, true);\n";
				method += "  }\n\n";
				
				indirectMethods.put(uri, method);
			}
			

		}
		
		if(! anno.equals("")){
			anno = "@IndirectProperty({" + anno + "})";
		}
		
		return anno;
	}
	
	private String getUnstoredAnnotation(Element property, Map<String, String> unstoredMethods, String parentPropTypeImpl) {
		String anno = "";
		NodeList nodeList = property.getElementsByTagName("unstoredProperty");
		boolean bFirst = true;
		for(int i = 0; i < nodeList.getLength(); i++){
			Element propEl = (Element) nodeList.item(i);
			
			String uri = propEl.getAttribute("uri");
			
			if(bFirst){
				anno = "\"" + uri + "\"";
				bFirst = false;
			} else 
				anno += ",\"" + uri + "\"";

			// indirect methods
			if(propEl.hasAttribute("name")){
				String propName = propEl.getAttribute("name").substring(0,1).toLowerCase() + propEl.getAttribute("name").substring(1);
				String propNameUCFirst = propName.substring(0,1).toUpperCase() + propName.substring(1);
				
				String method = "  public " + parentPropTypeImpl + " get" + propNameUCFirst + "(){\n";
				method += "    return (" + parentPropTypeImpl + ") this.getPropertyValue(\"" + uri + "\", false, true);\n";
				method += "  }\n\n";
				
				unstoredMethods.put(uri, method);
			}
		}
		
		if(! anno.equals("")){
			anno = "@UnstoredProperty({" + anno + "})";
		}
		
		return anno;
	}
	
	private String getPropertyAnnotation(String propType, String uri, String collection) {
		switch(getPropertyAnnotationType(propType, uri, collection)){
		case SIMPLE_URI_PROPERTY:
			return "@SimpleURIProperty(\"" + uri + "\")";
		case SIMPLE_LITERAL_PROPERTY:
			return "@SimpleLiteralProperty(\n" +
					"    value = \"" + uri + "\",\n" +
					"    datatypeType = \"" + "javaprimitive" + "\",\n" +
					"    javaType = \"" + propType + "\"\n" +
					"  )";
		case SIMPLE_COMPLEX_PROPERTY:
			return "@ComplexProperty(\"" + uri + "\")";
		case BAG_URI_PROPERTY:
			return "@BagURIProperty(\"" + uri + "\")";
		case BAG_LITERAL_PROPERTY:
			return "@BagLiteralProperty(\n" +
					"    value = \"" + uri + "\",\n" +
					"    datatypeType = \"" + "javaprimitive" + "\",\n" +
					"    javaType = \"" + propType + "\"\n" +
					"  )";
		case BAG_COMPLEX_PROPERTY:
			return "@BagComplexProperty(\"" + uri + "\")";
		}
		
		return "";
	}

	/**
	 * Returns the appropriate method name for returning a literals value.
	 * 
	 * @param property
	 * @return
	 */
	private String getLiteralGetMethod(Element property){
		switch(getLiteralType(property)){
		case LITERAL_TYPE_STRING:
			return "getString()";
		case LITERAL_TYPE_INTEGER:
			return "getInt()";
		case LITERAL_TYPE_FLOAT:
			return "getFloat()";
		case LITERAL_TYPE_DOUBLE:
			return "getDouble()";
		case LITERAL_TYPE_BOOLEAN:
			return "getBoolean()";
		}
		
		return null;
	}
	
	private int getLiteralType(Element property){
		String propType = property.getAttribute("type");
		return getLiteralType(propType);
	}
	
	private int getLiteralType(String propType) {
		propType = propType.toLowerCase();
		if(propType.equals("string"))
			return LITERAL_TYPE_STRING;
		if(propType.equals("int") || propType.equals("integer"))
			return LITERAL_TYPE_INTEGER;
		if(propType.equals("double"))
			return LITERAL_TYPE_DOUBLE;
		if(propType.equals("float"))
			return LITERAL_TYPE_FLOAT;
		if(propType.equals("boolean"))
			return LITERAL_TYPE_BOOLEAN;
		
		throw new IllegalArgumentException("Unknown literal type: " + propType);
	}


	private int getPropertyAnnotationType(Element property ){
		String propType = property.getAttribute("type");
		String uri = property.getAttribute("uri");
		String collection = property.getAttribute("collection").toLowerCase();
		
		return getPropertyAnnotationType(propType, uri, collection);
	}
	
	private int getPropertyAnnotationType(String propType, String uri, String collection ){
		if(null != collection){
			if(collection.equals(""))
				collection = null;
			else
				collection = collection.toLowerCase();
		}
		
		if(entityNames.contains(propType) || propType.equals("GabotoEntity")){
			if(null == collection)
				return SIMPLE_URI_PROPERTY;
			else if(collection.equals("bag"))
				return BAG_URI_PROPERTY;
		}
		
		try {
			String beansPackage = GabotoBean.class.getPackage().getName();
			Class<?> clazz = Class.forName(beansPackage + "." + propType);
			if(clazz.newInstance() instanceof GabotoBean)
				if(null == collection)
					return SIMPLE_COMPLEX_PROPERTY;
				else if(collection.equals("bag"))
					return BAG_COMPLEX_PROPERTY;
		} catch (ClassNotFoundException e) {
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		if(null == collection)
			return SIMPLE_LITERAL_PROPERTY;
		else if(collection.equals("bag"))
			return BAG_LITERAL_PROPERTY;
		
		throw new RuntimeException("no property annotation found for: " + propType );
	}

}
