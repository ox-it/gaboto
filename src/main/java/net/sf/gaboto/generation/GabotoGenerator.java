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

package net.sf.gaboto.generation;

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
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import net.sf.gaboto.node.GabotoBean;
import net.sf.gaboto.util.XMLUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
public class GabotoGenerator {

	private File config = null;
	private File outputDir;
	private File entityOutputDir;
	private File beanOutputDir;

	private String packageName = null;
	private String entitiesPackageName = null;
	private String beansPackageName = null;
	private String lookupClassName = null;

	private String entityClassNames = "";

	private Collection<String> entityNames = new HashSet<String>();
	private Collection<String> entityTypes = new HashSet<String>();
	private Map<String, String> entityClassLookup = new HashMap<String, String>();
	private Map<String, String> beanClassLookup = new HashMap<String, String>();
	private Collection<String> beanNames;

	// Type 2 uri
	private Map<String, String> entityTypeURILookup = new HashMap<String, String>();

	public final static int LITERAL_TYPE_STRING = 1;
	public final static int LITERAL_TYPE_INTEGER = 2;
	public final static int LITERAL_TYPE_FLOAT = 3;
	public final static int LITERAL_TYPE_DOUBLE = 4;
	public final static int LITERAL_TYPE_BOOLEAN = 5;
	public final static int LITERAL_TYPE_DATETIME = 6;
	public final static int LITERAL_TYPE_DATE = 7;
	

	public final static int SIMPLE_LITERAL_PROPERTY = 1;
	public final static int SIMPLE_URI_PROPERTY = 2;
	public final static int SIMPLE_COMPLEX_PROPERTY = 3;
	public final static int SIMPLE_RESOURCE_PROPERTY = 4;
	public final static int BAG_LITERAL_PROPERTY = 5;
	public final static int BAG_URI_PROPERTY = 6;
	public final static int BAG_COMPLEX_PROPERTY = 7;
	public final static int BAG_RESOURCE_PROPERTY = 8;

	public GabotoGenerator(File config, File outputDir, String packageName) {
		String packageRelativeDirectoryName = packageName.replace('.', '/');
		this.config = config;
		this.entityOutputDir = new File(outputDir, packageRelativeDirectoryName + "/entities");
		this.beanOutputDir = new File(outputDir, packageRelativeDirectoryName + "/beans");
		this.outputDir = new File(outputDir, packageRelativeDirectoryName + "/");

		this.entitiesPackageName = packageName + ".entities";
		this.beansPackageName = packageName + ".beans";
		this.packageName = packageName ;
	}

	public void run() throws ParserConfigurationException, SAXException, IOException {
		// load document
		Document doc = XMLUtils.readInputFileIntoJAXPDoc(config);
		
		beanNames = getBeanNames(doc);

		Element root = doc.getDocumentElement();
		NodeList children = root.getChildNodes();

		// load entity types first as they can be referred to in any order
		for (int i = 0; i < children.getLength(); i++) {
			if (!(children.item(i) instanceof Element))
				continue;
			Element el = (Element)children.item(i);
			System.err.println("Name " + el.getNodeName());
			if (el.getNodeName().equals("GabotoEntities")) {
				NodeList entities = el.getChildNodes();

				// now generate the entities
				for (int j = 0; j < entities.getLength(); j++) {
					if (!(entities.item(j) instanceof Element))
						continue;
					Element entityEl = (Element) entities.item(j);

					readConfigFile(entityEl);
				}
			} else if (el.getNodeName().equals("config")) {
				NodeList entities = el.getChildNodes();

				// now generate the entities
				for (int j = 0; j < entities.getLength(); j++) {
					if (!(entities.item(j) instanceof Element))
						continue;
					Element entityEl = (Element) entities.item(j);
					if (entityEl.getNodeName().equals("lookupClass")) {
						String fullClassName = entityEl.getAttribute("class"); 
						this.lookupClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
					}
				}
			}
		}

		// Generate classes from loaded entities
		for (int i = 0; i < children.getLength(); i++) {
			if (!(children.item(i) instanceof Element))
				continue;
			// cast
			Element el = (Element) children.item(i);

			if (el.getNodeName().equals("GabotoEntities")) {
				NodeList entities = el.getChildNodes();

				// generate the entities
				for (int j = 0; j < entities.getLength(); j++) {
					if (!(entities.item(j) instanceof Element))
						continue;
					Element entityEl = (Element) entities.item(j);
					generateEntity(entityEl, "GabotoEntity");
				}
			} else if (el.getNodeName().equals("GabotoBeans")) {
				NodeList beans = el.getChildNodes();

				// generate the beans
				for (int j = 0; j < beans.getLength(); j++) {
					if (!(beans.item(j) instanceof Element))
						continue;
					Element beanEl = (Element) beans.item(j);
					generateBean(beanEl, "GabotoBean", new ArrayList<String>());
				}
			}
		}

		generateLookup();

	}

	private void readConfigFile(Element entityEl) {
		entityNames.add(entityEl.getAttribute("name"));
		entityTypeURILookup.put(entityEl.getAttribute("name"), entityEl.getAttribute("type"));

		// recursion
		NodeList children = entityEl.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (!(children.item(i) instanceof Element))
				continue;

			if (children.item(i).getNodeName().equals("GabotoEntities")) {
				NodeList entities = children.item(i).getChildNodes();
				for (int j = 0; j < entities.getLength(); j++) {
					if (!(entities.item(j) instanceof Element))
						continue;

					if (entities.item(j).getNodeName().equals("GabotoEntity")) {
						readConfigFile((Element) entities.item(j));
					}
				}
			}
		}
	}

	private void generateBean(Element beanEl, String extendsClassName, List<String> inheritedPropertyNames) {
		if (!beanEl.getNodeName().equals("GabotoBean"))
			return;

		JavaText classText = new JavaText();

		if (extendsClassName.equals("GabotoBean"))
			classText.addImport("net.sf.gaboto.node." + extendsClassName);
		else 
			classText.addImport(beansPackageName + "." + extendsClassName);

		// get name
		String name = beanEl.getAttribute("name");
		System.out.println("Generate java file for bean : " + name);

		// type definition
		String beanType = beanEl.getAttribute("type");
		String typeDef = "\n" + "  @Override\n" + "  public String getType(){\n" + "    return \"" + beanType + "\";\n"
		+ "  }\n" + "\n";

		// loadEntityMethod
		boolean bBeanHasProperty = false;
		String loadBeanMethod = "  public void loadFromResource(Resource res, GabotoSnapshot snapshot, EntityPool pool) {\n";
		loadBeanMethod += "    super.loadFromResource(res, snapshot, pool);\n";
		loadBeanMethod += "    Statement stmt;\n\n";

		// custom methods
		String customMethods = "";

		// get properties
		String propertyDefinitions = "";
		String methodDefinitions = "";
		List<String> propertyNames = new ArrayList<String>();

		NodeList children = beanEl.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (!(children.item(i) instanceof Element))
				continue;
			if (children.item(i).getNodeName().equals("properties")) {
				classText.addImport("net.sf.gaboto.node.pool.EntityPool");
				classText.addImport("net.sf.gaboto.node.annotation.SimpleLiteralProperty");
				classText.addImport("net.sf.gaboto.GabotoSnapshot");
				classText.addImport("com.hp.hpl.jena.rdf.model.Literal"); 
				classText.addImport("com.hp.hpl.jena.rdf.model.Resource"); 
				classText.addImport("com.hp.hpl.jena.rdf.model.Statement");

				NodeList properties = children.item(i).getChildNodes();
				for (int j = 0; j < properties.getLength(); j++) {
					if (!(properties.item(j) instanceof Element))
						continue;

					if (properties.item(j).getNodeName().equals("property")) {
						bBeanHasProperty = true;

						// cast property
						Element property = (Element) properties.item(j);

						PropertyJavaText text = createEntityJavaText(property, classText, null, null, null, null);

						propertyDefinitions += text.getPropertyDefinitions();
						methodDefinitions += text.getMethodDefinitions();
						loadBeanMethod += text.getLoadMethod();

						propertyNames.add(property.getAttribute("name"));
					}
				}
			} else if (children.item(i).getNodeName().equals("customMethods")) {
				NodeList methods = children.item(i).getChildNodes();
				for (int j = 0; j < methods.getLength(); j++) {
					if (!(methods.item(j) instanceof Element))
						continue;

					if (methods.item(j).getNodeName().equals("method")) {
						customMethods += ((Element) methods.item(j)).getTextContent();
					}
				}
			} else if (children.item(i).getNodeName().equals("GabotoBeans")) {
				List<String> newInheritedPropertyNames = new ArrayList<String>();
				newInheritedPropertyNames.addAll(propertyNames);
				newInheritedPropertyNames.addAll(inheritedPropertyNames);


				NodeList entities = children.item(i).getChildNodes();
				for (int j = 0; j < entities.getLength(); j++) {
					if (!(entities.item(j) instanceof Element))
						continue;

					if (entities.item(j).getNodeName().equals("GabotoBean")) {
						generateBean((Element) entities.item(j), name, newInheritedPropertyNames);
					}
				} }
		}
		
		loadBeanMethod += "  }\n";

		// load entity method
		String clazz = "package " + beansPackageName + ";\n\n";
		clazz += classText.getImports() + "\n\n";
		clazz +=     // class comment
			"/**\n"
			+ " * Gaboto generated bean.\n"
			+ " * @see " + this.getClass().getCanonicalName() + "\n"
			+ " */\n";
		clazz += "public class ";
		clazz += name + " extends " + extendsClassName + " {\n";
		clazz += propertyDefinitions;
		clazz += typeDef;
		clazz += methodDefinitions + "\n\n";
		clazz += customMethods + "\n\n";
		if (bBeanHasProperty)
			clazz += loadBeanMethod;
		clazz += "  public String toString() {\n" + "    return ";
		boolean seenOne = false;
		List<String> newInheritedPropertyNames = new ArrayList<String>();
		newInheritedPropertyNames.addAll(propertyNames);
		newInheritedPropertyNames.addAll(inheritedPropertyNames);
		for (String pName : newInheritedPropertyNames) {
			if (seenOne)
				clazz += " + \", \" + ";
			seenOne = true;
			clazz += "this." + pName;
		}
		// this.streetAddress + \", \" + this.postCode;\n" +
		clazz += " ;\n" + "  }\n";

		clazz += "}";

		beanClassLookup.put(beanType, name);

		// write file
		try {
			File outputFile = new File(beanOutputDir.getAbsolutePath() + File.separator + name + ".java");
			System.out.println("Write java class to: " + outputFile.getAbsolutePath());
			BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
			out.write(clazz);
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void generateEntity(Element entityEl, String extendsClassName) {
		if (!entityEl.getNodeName().equals("GabotoEntity"))
			return;

		JavaText cText = new JavaText();
		cText.addImport("java.lang.reflect.Method");
		cText.addImport("java.util.HashMap");
		cText.addImport("java.util.List");
		cText.addImport("java.util.Map");


		if (extendsClassName.equals("GabotoEntity"))
			cText.addImport("net.sf.gaboto.node." + extendsClassName);
		else 
			cText.addImport(entitiesPackageName + "." + extendsClassName);



		// get name
		String entityName = entityEl.getAttribute("name");
		System.out.println("Generate entity for file: " + entityName);


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

		boolean entityHasEntity = false;
		boolean entityHasProperty = false;
		boolean entityHasPassiveProperty = false;

		String loadEntityMethod = 
			"  public void loadFromSnapshot(Resource res, GabotoSnapshot snapshot, EntityPool pool) {\n" + 
			"    super.loadFromSnapshot(res, snapshot, pool);\n" + 
			"    Statement stmt;\n\n";

		// passive entity requests
		String passiveEntityRequests = 
			"  public Collection<PassiveEntitiesRequest> getPassiveEntitiesRequest(){\n" + 
			"    Collection<PassiveEntitiesRequest> requests = super.getPassiveEntitiesRequest();\n" + 
			"    if(requests == null)\n" +
			"      requests = new HashSet<PassiveEntitiesRequest>();\n";

		// custom methods
		String customMethods = "";

		// indirect methods
		Map<String, List<String>> indirectMethodLookup = new HashMap<String, List<String>>();
		Map<String, String> indirectMethods = new HashMap<String, String>();

		// unstored methods
		Map<String, String> unstoredMethods = new HashMap<String, String>();

		// get properties
		String propertyDefinitions = "";
		String methodDefinitions = "";
		NodeList children = entityEl.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (!(children.item(i) instanceof Element))
				continue;
			if (children.item(i).getNodeName().equals("properties")) {
				NodeList properties = children.item(i).getChildNodes();
				for (int j = 0; j < properties.getLength(); j++) {
					if (!(properties.item(j) instanceof Element))
						continue;

					if (properties.item(j).getNodeName().equals("property")) {
						entityHasProperty = true;
						cText.addImport("com.hp.hpl.jena.rdf.model.Resource");
						cText.addImport("com.hp.hpl.jena.rdf.model.Statement");
						cText.addImport("net.sf.gaboto.GabotoSnapshot");
						// cast property
						Element property = (Element) properties.item(j);

						PropertyJavaText text = createEntityJavaText(property, cText, unstoredMethods, indirectMethods,
								indirectMethodLookup, entityName);
						propertyDefinitions += text.getPropertyDefinitions();
						methodDefinitions += text.getMethodDefinitions();
						loadEntityMethod += text.getLoadMethod();
					} else if (properties.item(j).getNodeName().equals("passiveProperty")) {
						entityHasPassiveProperty = true;
						cText.addImport("net.sf.gaboto.node.pool.PassiveEntitiesRequest");
						cText.addImport("net.sf.gaboto.node.annotation.PassiveProperty");

						// cast property
						Element property = (Element) properties.item(j);

						String[] processedProperty = processPassiveProperty(property, cText, indirectMethods, indirectMethodLookup,
								entityName);
						propertyDefinitions += processedProperty[1];
						methodDefinitions += processedProperty[2];
						passiveEntityRequests += processedProperty[3];
					}
				}
			} else if (children.item(i).getNodeName().equals("GabotoEntities")) {
				entityHasEntity = true;
				NodeList entities = children.item(i).getChildNodes();
				for (int j = 0; j < entities.getLength(); j++) {
					if (!(entities.item(j) instanceof Element))
						continue;

					if (entities.item(j).getNodeName().equals("GabotoEntity")) {
						generateEntity((Element) entities.item(j), entityName);
					}
				}
			} else if (children.item(i).getNodeName().equals("customMethods")) {
				NodeList methods = children.item(i).getChildNodes();
				for (int j = 0; j < methods.getLength(); j++) {
					if (!(methods.item(j) instanceof Element))
						continue;

					if (methods.item(j).getNodeName().equals("method")) {
						customMethods += ((Element) methods.item(j)).getTextContent();
					}
				}
			}
		}

		// indirect properties
		String indirectPropertyMethods = "";
		for (String method : indirectMethods.values())
			indirectPropertyMethods += method;

		// unstored properties
		String unstoredPropertyMethods = "";
		for (String method : unstoredMethods.values())
			unstoredPropertyMethods += method;


		// indirect property position lookup
		String indirectPropertyLookupTable = "  private static Map<String, List<Method>> indirectPropertyLookupTable;\n";
		indirectPropertyLookupTable += "  static{\n";
		indirectPropertyLookupTable += "    indirectPropertyLookupTable = new HashMap<String, List<Method>>();\n";

		if (indirectMethodLookup.size() > 0) {
			cText.addImport("java.util.ArrayList");
			cText.addImport("net.sf.gaboto.GabotoRuntimeException");
			indirectPropertyLookupTable += "    List<Method> list;\n\n";
			indirectPropertyLookupTable += "    try {\n";
			for (Entry<String, List<String>> entry : indirectMethodLookup.entrySet()) {
				indirectPropertyLookupTable += "      list = new ArrayList<Method>();\n";
				for (String property : entry.getValue()) {
					if (property != null)
						indirectPropertyLookupTable += "      list.add(" + property + ");\n";
				}
				indirectPropertyLookupTable += "      indirectPropertyLookupTable.put(\"" + entry.getKey() + "\", list);\n\n";
			}
			indirectPropertyLookupTable += "    } catch (Exception e) {\n";
			indirectPropertyLookupTable += "      throw new GabotoRuntimeException(e);\n";
			indirectPropertyLookupTable += "    }\n";
		}
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
		boolean abstractClass = entityEl.hasAttribute("abstract") ? entityEl.getAttribute("abstract").equals("true")
				: false;
		if (entityHasEntity | entityHasPassiveProperty)
			cText.addImport("net.sf.gaboto.node.GabotoEntity");

		if (customMethods.contains("StaticProperty"))
			cText.addImport("net.sf.gaboto.node.annotation.StaticProperty");

		if (entityHasPassiveProperty || entityHasProperty)
			cText.addImport("net.sf.gaboto.node.pool.EntityPool");

		// build it all together
		String clazz = "package " + entitiesPackageName + ";\n\n";
		clazz += cText.getImports() + "\n\n";
		clazz +=     // class comment
			"/**\n"
			+ " * Gaboto generated Entity.\n"
			+ " * @see " + this.getClass().getCanonicalName() + "\n"
			+ " */\n";


		if (abstractClass)
			clazz += "abstract ";
		clazz += "public class ";
		clazz += entityName + " extends " + extendsClassName + " {\n";
		clazz += propertyDefinitions;
		clazz += indirectPropertyLookupTable;
		clazz += typeDef;
		clazz += methodDefinitions + "\n\n";
		clazz += indirectPropertyMethods;
		clazz += unstoredPropertyMethods;
		clazz += customMethods + "\n\n";
		if (entityHasPassiveProperty)
			clazz += passiveEntityRequests + "\n\n";
		if (entityHasProperty)
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
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private PropertyJavaText createEntityJavaText(Element property, JavaText classText, Map<String, String> unstoredMethods,
			Map<String, String> indirectMethods, Map<String, List<String>> indirectMethodLookup, String entityName) {
		PropertyJavaText pt = new PropertyJavaText();

		pt.propName = property.getAttribute("name").substring(0, 1).toLowerCase()
		+ property.getAttribute("name").substring(1);
		pt.propNameUCFirst = pt.propName.substring(0, 1).toUpperCase() + pt.propName.substring(1);
		pt.propType = property.getAttribute("type");
		pt.propTypeItem = property.getAttribute("type");
		pt.collection = property.getAttribute("collection").toLowerCase();
		pt.uri = property.getAttribute("uri");

		// real prop type
		pt.realPropTypeInterface = pt.propType;
		pt.realPropTypeImpl = pt.propType;
		if (pt.propType.equals("Resource")) {
			pt.realPropTypeInterface = "String";	
			pt.realPropTypeImpl = "String";	
			pt.propTypeItem = "String";	
		}
		if (pt.collection.equals("bag")) {
			pt.realPropTypeInterface = "Collection<" + pt.propTypeItem + ">";
			pt.realPropTypeImpl = "HashSet<" + pt.propTypeItem + ">";
		}

		pt.getMethodName = "get" + pt.propNameUCFirst;

		// annotations
		pt.propertyAnnotation = getPropertyAnnotation(pt.propType, pt.uri, pt.collection);
		pt.indirectAnnotation = getIndirectAnnotation(property, indirectMethods, indirectMethodLookup, entityName,
				pt.getMethodName);
		if (!pt.indirectAnnotation.equals("")) { 
			classText.addImport("net.sf.gaboto.node.annotation.IndirectProperty");      
		}
		pt.unstoredAnnotation = getUnstoredAnnotation(property, unstoredMethods, pt.realPropTypeImpl);
		if (!pt.unstoredAnnotation.equals(""))
			classText.addImport("net.sf.gaboto.node.annotation.UnstoredProperty");

		// property definition
		pt.propertyDefinitions += "  protected " + pt.realPropTypeInterface + " " + pt.propName + ";\n";

		// add additional imports
		//pt.importDefinitions += addBeanImport(pt.propType, classText);

		addBeanImport(pt.propType, classText);

		// get method
		if (!pt.unstoredAnnotation.equals(""))
			pt.methodDefinitions += "  " + pt.unstoredAnnotation + "\n";
		if (!pt.indirectAnnotation.equals(""))
			pt.methodDefinitions += "  " + pt.indirectAnnotation + "\n";
		pt.methodDefinitions += "  " + pt.propertyAnnotation + "\n";

		switch (getPropertyAnnotationType(property)) {
		case BAG_URI_PROPERTY:

		case SIMPLE_URI_PROPERTY:
			pt.methodDefinitions += 
				getGetMethodForDirectReference(pt.realPropTypeInterface, pt.getMethodName, pt.propName);
			break;
		default:
			pt.methodDefinitions += 
				getGetMethod(pt.realPropTypeInterface, pt.getMethodName, pt.propName);
			break;
		}

		// set method
		String setMethodName = "set" + pt.propNameUCFirst;
		pt.methodDefinitions += "  " + pt.propertyAnnotation + "\n";
		switch (getPropertyAnnotationType(property)) {
		case SIMPLE_URI_PROPERTY:
			pt.methodDefinitions += 
				getSetMethodForSimpleURIProperty("public", setMethodName, pt.realPropTypeInterface, pt.propName,
						pt.propName);
			break;
		case BAG_URI_PROPERTY:
			pt.methodDefinitions += 
				getSetMethodForBagURIProperty("public", setMethodName, pt.realPropTypeInterface, pt.propName,
						pt.propName);
			break;
		default:
			pt.methodDefinitions += 
				getSetMethod("public", setMethodName, pt.realPropTypeInterface, pt.propName, pt.propName);
			break;
		}

		// add method
		String addMethodName = "add";
		if (pt.collection.equals("bag")) {
			String parameterName = "";
			if (pt.propNameUCFirst.endsWith("s")) {
				addMethodName += pt.propNameUCFirst.substring(0, pt.propNameUCFirst.length() - 1);
				parameterName = pt.propName.substring(0, pt.propName.length() - 1);
			} else {
				addMethodName += pt.propNameUCFirst;
				parameterName = pt.propName;
			}

			switch (getPropertyAnnotationType(property)) {
			case BAG_URI_PROPERTY:
				pt.methodDefinitions += getAddMethodForBagURI("public", addMethodName, pt.propType, 
						pt.realPropTypeImpl,
						parameterName, pt.propName);
				break;
			default:
				pt.methodDefinitions += 
					getAddMethod("public", addMethodName, pt.propTypeItem, pt.realPropTypeImpl, parameterName,
							pt.propName);
				break;
			}

		}
		System.err.println("Prop type " + pt.propType);

		// load entity snippet
		pt.loadMethod = 
			getLoadPropertySnippet(classText, property, pt.uri, pt.propType, pt.realPropTypeInterface, pt.realPropTypeImpl,
					pt.propName, setMethodName, addMethodName);

		return pt;
	}

	private String[] processPassiveProperty(Element property, JavaText cText, Map<String, String> indirectMethods,
			Map<String, List<String>> indirectMethodLookup, String entityName) {
		String importDefinitions = "";
		String propertyDefinitionss = "";
		String methodDefinitions = "";
		String passiveEntityRequests = "";

		String relationshipType = property.getAttribute("relationshipType");
		String propType = property.getAttribute("type");
		String uri = property.getAttribute("uri");
		String propName = property.getAttribute("name");
		String propNameUCFirst = propName.substring(0, 1).toUpperCase() + propName.substring(1);

		// build annotation
		String annotation = 
			"  @PassiveProperty(\n" + 
			"    uri = \"" + uri + "\",\n" + 
			"    entity = \"" + propType + "\"\n" + 
			"  )\n";

		String getMethodName = "", setMethodName = "", addMethodName = "";

		// what relationship type do we have
		if (relationshipType.equals("1:N") || relationshipType.equals("N:M")) {
			cText.addImport("java.util.Collection");
			cText.addImport("java.util.HashSet");
			// we can have N propTypes
			String realPropTypeInterface = "Collection<" + propType + ">";
			String realPropTypeImpl = "HashSet<" + propType + ">";

			// add member property
			propertyDefinitionss += "  private " + realPropTypeInterface + " " + propName + ";\n";

			// generate get/set/add method

			// get method
			getMethodName = "get" + propNameUCFirst;

			// indirect annotation
			String indirectAnnotation = getIndirectAnnotation(property, indirectMethods, indirectMethodLookup, entityName,
					getMethodName);

			if (!"".equals(indirectAnnotation))
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
			if (propNameUCFirst.endsWith("s")) { // FIXME Hack
				addMethodName += propNameUCFirst.substring(0, propNameUCFirst.length() - 1);
				parameterName = propName.substring(0, propName.length() - 1);
			} else {
				addMethodName += propNameUCFirst;
				parameterName = propName;
			}
			methodDefinitions += getAddMethod("private", addMethodName, propType, realPropTypeImpl, parameterName,
					propName);

		} else {
			throw new RuntimeException("Illegal relationship type: " + relationshipType);
		}

		// requests
		if (relationshipType.equals("1:N")) {
			// request
			passiveEntityRequests += "    requests.add(new PassiveEntitiesRequest(){\n";
			passiveEntityRequests += "      public String getType() {\n";
			passiveEntityRequests += "        return \"" + entityTypeURILookup.get(propType) + "\";\n";
			passiveEntityRequests += "      }\n\n";
			passiveEntityRequests += "      public String getUri() {\n";
			passiveEntityRequests += "        return \"" + uri + "\";\n";
			passiveEntityRequests += "      }\n\n";
			passiveEntityRequests += "      public int getCollectionType() {\n";
			passiveEntityRequests += "        return EntityPool.PASSIVE_PROPERTY_COLLECTION_TYPE_NONE;\n";
			passiveEntityRequests += "      }\n\n";
			passiveEntityRequests += "      public void passiveEntityLoaded(GabotoEntity entity) {\n";
			passiveEntityRequests += "        " + addMethodName + "((" + propType + ")entity);\n";
			passiveEntityRequests += "      }\n";
			passiveEntityRequests += "    });\n";
		} else if (relationshipType.equals("N:M")) {
			// request
			passiveEntityRequests += "    requests.add(new PassiveEntitiesRequest(){\n";
			passiveEntityRequests += "      public String getType() {\n";
			passiveEntityRequests += "        return \"" + entityTypeURILookup.get(propType) + "\";\n";
			passiveEntityRequests += "      }\n\n";
			passiveEntityRequests += "      public String getUri() {\n";
			passiveEntityRequests += "        return \"" + uri + "\";\n";
			passiveEntityRequests += "      }\n\n";
			passiveEntityRequests += "      public int getCollectionType() {\n";
			passiveEntityRequests += "        return EntityPool.PASSIVE_PROPERTY_COLLECTION_TYPE_BAG;\n";
			passiveEntityRequests += "      }\n\n";
			passiveEntityRequests += "      public void passiveEntityLoaded(GabotoEntity entity) {\n";
			passiveEntityRequests += "        " + addMethodName + "((" + propType + ")entity);\n";
			passiveEntityRequests += "      }\n";
			passiveEntityRequests += "    });\n";
		} else throw new RuntimeException("Unrecognised relationship type" + relationshipType);

		return new String[] { importDefinitions, propertyDefinitionss, methodDefinitions, passiveEntityRequests };
	}

	private String getGetMethod(String returnType, String methodName, String memberName) {
		String methodDefinition = "";

		methodDefinition += "  public " + returnType + " " + methodName + "(){\n";
		methodDefinition += "    return this." + memberName + ";\n";
		methodDefinition += "  }\n\n";

		return methodDefinition;
	}

	private String getGetMethodForDirectReference(String returnType, String methodName, String memberName) {
		String methodDefinition = "";

		methodDefinition += "  public " + returnType + " " + methodName + "(){\n";
		methodDefinition += "    if(! this.isDirectReferencesResolved())\n";
		methodDefinition += "      this.resolveDirectReferences();\n";
		methodDefinition += "    return this." + memberName + ";\n";
		methodDefinition += "  }\n\n";

		return methodDefinition;
	}

	private String getSetMethod(String visibility, String methodName, String parameterType, String parameterName,
			String memberName) {
		String methodDefinition = "";

		if (visibility.equals("package"))
			visibility = "";

		methodDefinition += "  " + visibility + " void " + methodName + "(" + parameterType + " " + parameterName + "){\n";
		methodDefinition += "    this." + memberName + " = " + parameterName + ";\n";
		methodDefinition += "  }\n\n";

		return methodDefinition;
	}

	private String getSetMethodForSimpleURIProperty(String visibility, String methodName, String parameterType,
			String parameterName, String memberName) {
		String methodDefinition = "";

		if (visibility.equals("package"))
			visibility = "";

		methodDefinition += "  " + visibility + " void " + methodName + "(" + parameterType + " " + parameterName + "){\n";
		methodDefinition += "    if( " + parameterName + " != null )\n";
		methodDefinition += "      this.removeMissingReference( " + parameterName + ".getUri() );\n";
		methodDefinition += "    this." + memberName + " = " + parameterName + ";\n";
		methodDefinition += "  }\n\n";

		return methodDefinition;
	}

	private String getSetMethodForBagURIProperty(String visibility, String methodName, String parameterType,
			String parameterName, String memberName) {
		String methodDefinition = "";

		if (visibility.equals("package"))
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

	private String getAddMethod(String visibility, String methodName, String propTypeItem, String realPropTypeImpl,
			String parameterName, String memberName) {
		String methodDefinition = "";

		if (visibility.equals("package"))
			visibility = "";

		methodDefinition += "  " + visibility + " void " + methodName + "(" + propTypeItem + " " + parameterName + "P){\n";
		methodDefinition += "    if(this." + memberName + " == null)\n";
		methodDefinition += "      set" + ucFirstLetter(memberName) + "( new " + realPropTypeImpl + "() );\n";
		methodDefinition += "    this." + memberName + ".add(" + parameterName + "P);\n";
		methodDefinition += "  }\n\n";

		return methodDefinition;
	}

	String ucFirstLetter(String s) { 
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}
	private String getAddMethodForBagURI(String visibility, String methodName, String propType,
			String realPropTypeImpl, String parameterName, String memberName) {
		String methodDefinition = "";

		if (visibility.equals("package"))
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

	private String getLoadPropertySnippet(JavaText cText, Element property, String uri, String propType, String realPropTypeInterface,
			String realPropTypeImpl, String propertyName, String setMethodName, String addMethodName) {
		String loadEntity = "";

		System.err.println("Property " + propType + " - " + realPropTypeImpl);
		System.err.println("Property " + propertyName + " - " + getPropertyAnnotationType(property));
		switch (getPropertyAnnotationType(property)) {
		case SIMPLE_LITERAL_PROPERTY:
			cText.addImport("com.hp.hpl.jena.rdf.model.Literal");
			cText.addImport("net.sf.gaboto.node.annotation.SimpleLiteralProperty");
			cText.addImport("net.sf.gaboto.node.pool.EntityPool");
			loadEntity += "    // Load SIMPLE_LITERAL_PROPERTY " + propertyName + "\n";
			loadEntity += "    stmt = res.getProperty(snapshot.getProperty(\"" + uri + "\"));\n";
			loadEntity += "    if(stmt != null && stmt.getObject().isLiteral())\n";
			loadEntity += "      this." + setMethodName + "(((Literal)stmt.getObject())." + getLiteralGetMethod(property)
			+ ");\n";
			break;
		case SIMPLE_URI_PROPERTY:
			cText.addImport("net.sf.gaboto.node.annotation.SimpleURIProperty");
			cText.addImport("net.sf.gaboto.node.pool.EntityExistsCallback");
			cText.addImport("net.sf.gaboto.node.pool.EntityPool");
			cText.addImport("net.sf.gaboto.node.GabotoEntity");
			loadEntity += "    // Load SIMPLE_URI_PROPERTY " + propertyName + "\n";
			loadEntity += "    stmt = res.getProperty(snapshot.getProperty(\"" + uri + "\"));\n";
			loadEntity += "    if(stmt != null && stmt.getObject().isResource()){\n";
			loadEntity += "      Resource missingReference = (Resource)stmt.getObject();\n";

			loadEntity += "      EntityExistsCallback callback = new EntityExistsCallback(){\n";
			loadEntity += "        public void entityExists(EntityPool p, GabotoEntity entity) {\n";
			loadEntity += "          " + setMethodName + "((" + realPropTypeInterface + ")entity);\n";
			loadEntity += "        }\n";
			loadEntity += "      };\n";

			loadEntity += "      this.addMissingReference(missingReference, callback);\n";
			loadEntity += "    }\n";
			break;
		case SIMPLE_COMPLEX_PROPERTY:
			cText.addImport("com.hp.hpl.jena.rdf.model.Resource");
			cText.addImport("net.sf.gaboto.node.annotation.ComplexProperty");
			cText.addImport(beansPackageName + "." + propType);
			cText.addImport(packageName + "." + lookupClassName);
			loadEntity += "    // Load SIMPLE_COMPLEX_PROPERTY " + propertyName + "\n";
			loadEntity += "    stmt = res.getProperty(snapshot.getProperty(\"" + uri + "\"));\n";
			loadEntity += "    if(stmt != null && stmt.getObject().isAnon()){\n";
			loadEntity += "      " + realPropTypeInterface + " bean = new " + realPropTypeInterface + "();\n";
			loadEntity += "      " + "bean.loadFromResource((Resource)stmt.getObject(), snapshot, pool);\n";
			loadEntity += "      " + setMethodName + "(bean);\n";
			loadEntity += "    }\n";
			break;
		case SIMPLE_RESOURCE_PROPERTY:
			cText.addImport("net.sf.gaboto.node.annotation.ResourceProperty");
			loadEntity += "    // Load SIMPLE_RESOURCE_PROPERTY " + propertyName + "\n";
			loadEntity += "    stmt = res.getProperty(snapshot.getProperty(\"" + uri + "\"));\n";
			loadEntity += "    if(stmt != null && stmt.getObject().isURIResource()){\n";
			loadEntity += "      this." + setMethodName + "(((Resource) stmt.getObject()).getURI());\n";
			loadEntity += "    }\n";
			break;
	case BAG_URI_PROPERTY:
			cText.addImport("java.util.Collection");
			cText.addImport("java.util.HashSet");
			cText.addImport("com.hp.hpl.jena.rdf.model.RDFNode");
			cText.addImport("com.hp.hpl.jena.rdf.model.Resource");
			cText.addImport("com.hp.hpl.jena.rdf.model.StmtIterator");
			cText.addImport("net.sf.gaboto.node.pool.EntityPool");
			cText.addImport("net.sf.gaboto.node.pool.EntityExistsCallback");
			cText.addImport("net.sf.gaboto.node.annotation.BagURIProperty");
			loadEntity += "    // Load BAG_URI_PROPERTY " + propertyName + "\n";
			loadEntity += "    {\n";
			loadEntity += "        StmtIterator stmts = res.listProperties(snapshot.getProperty(\"" + uri + "\"));\n";
			loadEntity += "        while (stmts.hasNext()) {\n";
			loadEntity += "            RDFNode node = stmts.next().getObject();\n";
			loadEntity += "            if(! node.isResource())\n";
			loadEntity += "              throw new IllegalArgumentException(\"node should be a resource\");\n\n";

			loadEntity += "            Resource missingReference = (Resource)node;\n";

			loadEntity += "            EntityExistsCallback callback = new EntityExistsCallback(){\n";
			loadEntity += "              public void entityExists(EntityPool p, GabotoEntity entity) {\n";
			loadEntity += "                " + addMethodName + "((" + propType + ") entity);\n";
			loadEntity += "            }\n";
			loadEntity += "        };\n";

			loadEntity += "        this.addMissingReference(missingReference, callback);\n";
			loadEntity += "      }\n";
			loadEntity += "    }\n";
			break;
		case BAG_LITERAL_PROPERTY:
			cText.addImport("java.util.Collection");
			cText.addImport("java.util.HashSet");
			cText.addImport("com.hp.hpl.jena.rdf.model.RDFNode");
			cText.addImport("com.hp.hpl.jena.rdf.model.Literal");
			cText.addImport("com.hp.hpl.jena.rdf.model.StmtIterator");
			cText.addImport("net.sf.gaboto.node.annotation.BagLiteralProperty");
			loadEntity += "    // Load BAG_LITERAL_PROPERTY " + propertyName + "\n";
			loadEntity += "    {\n";
			loadEntity += "        StmtIterator stmts = res.listProperties(snapshot.getProperty(\"" + uri + "\"));\n";
			loadEntity += "        while (stmts.hasNext()) {\n";
			loadEntity += "            RDFNode node = stmts.next().getObject();\n";
			loadEntity += "            if(! node.isLiteral())\n";
			loadEntity += "              throw new IllegalArgumentException(\"node should be a literal\");\n\n";
			loadEntity += "            " + addMethodName + "(((Literal)node)." + getLiteralGetMethod(property) + ");\n";
			loadEntity += "        }\n";
			loadEntity += "    }\n";
			break;
		case BAG_COMPLEX_PROPERTY:
			cText.addImport("java.util.Collection");
			cText.addImport("java.util.HashSet");
			cText.addImport("net.sf.gaboto.node.annotation.BagComplexProperty");
			cText.addImport(beansPackageName + "." + propType);
			cText.addImport(packageName + "." + lookupClassName);
			cText.addImport("com.hp.hpl.jena.rdf.model.StmtIterator");
			cText.addImport("com.hp.hpl.jena.rdf.model.RDFNode");
			loadEntity += "    // Load BAG_COMPLEX_PROPERTY " + propertyName + "\n";
			loadEntity += "    {\n";
			loadEntity += "        StmtIterator stmts = res.listProperties(snapshot.getProperty(\"" + uri + "\"));\n";
			loadEntity += "        while (stmts.hasNext()) {\n";
			loadEntity += "            RDFNode node = stmts.next().getObject();\n";
			loadEntity += "            if(! node.isAnon())\n";
			loadEntity += "              throw new IllegalArgumentException(\"node should be a blank node\");\n\n";
			loadEntity += "            Resource anon_res = (Resource) node;\n";
			loadEntity += "            String type = anon_res.getProperty(snapshot.getProperty(\"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\")).getObject().toString();\n";
			loadEntity += "            " + propType + " prop;\n";
			loadEntity += "            try {\n";
			loadEntity += "                prop = (" + propType + ") (new OxpointsGabotoOntologyLookup()).getBeanClassFor(type).newInstance();\n";
			loadEntity += "            } catch (InstantiationException e) {\n";
			loadEntity += "                throw new GabotoRuntimeException();\n";
			loadEntity += "            } catch (IllegalAccessException e) {\n";
			loadEntity += "                throw new GabotoRuntimeException();\n";
			loadEntity += "            }\n";
			loadEntity += "            prop.loadFromResource(anon_res, snapshot, pool);\n";
			loadEntity += "            " + addMethodName + "(prop);\n";
			loadEntity += "        }\n\n";
			loadEntity += "    }\n";
			break;
		case BAG_RESOURCE_PROPERTY:
			cText.addImport("net.sf.gaboto.node.annotation.BagResourceProperty");
			loadEntity += "    // Load BAG_RESOURCE_PROPERTY " + propertyName + "\n";
			loadEntity += "    {\n";
			loadEntity += "        StmtIterator stmts = res.listProperties(snapshot.getProperty(\"" + uri + "\"));\n";
			loadEntity += "        while (stmts.hasNext()) {\n";
			loadEntity += "            RDFNode node = stmts.next().getObject();\n";
			loadEntity += "            if(node.isURIResource()){\n";
			loadEntity += "                this." + addMethodName + "(((Resource) node).getURI());\n";
			loadEntity += "            }\n";
			loadEntity += "        }\n";
			loadEntity += "    }\n";
			break;		}

		return loadEntity + "\n";
	}

	private void addBeanImport(String propType, JavaText classText) {
		System.err.println("Here:" + propType);
		if(propType.equals("String")){}
		else if (propType.equals("Int")) {}
		else if (propType.equals("Integer")) {}
		else if (propType.equals("Long")) {}
		else if (propType.equals("Double")) {}
		else if (propType.equals("Float")) {}
		else if (propType.equals("Boolean")) {}
		else { 
			// Check if this is a bean
			String beanName = beansPackageName + "." + propType;
			try {
				Class<?> clazz = Class.forName(beanName);
				if (clazz.newInstance() instanceof GabotoBean)
					classText.addImport(beanName);
				System.err.println(propType + " is a bean");
			} catch (ClassNotFoundException e) {
				// Simple types eg String
				System.err.println("Class not found " + beanName);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}


	private String getIndirectAnnotation(Element property, Map<String, String> indirectMethods,
			Map<String, List<String>> indirectMethodLookup, String entityName, String getMethodName) {
		String anno = "";
		NodeList nodeList = property.getElementsByTagName("indirectProperty");
		boolean bFirst = true;
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element propEl = (Element) nodeList.item(i);

			String uri = propEl.getAttribute("uri");

			if (bFirst) {
				anno = "\"" + uri + "\"";
				bFirst = false;
			} else
				anno += ",\"" + uri + "\"";

			// store position
			if (indirectMethodLookup != null) {
				int position = propEl.hasAttribute("n") ? Integer.valueOf(propEl.getAttribute("n")) - 1 : 0;
				if (!indirectMethodLookup.containsKey(uri))
					indirectMethodLookup.put(uri, new ArrayList<String>());

				List<String> list = indirectMethodLookup.get(uri);
				if (list.size() <= position) {
					for (int j = list.size(); j < position; j++)
						list.add(null);
					list.add(entityName + ".class.getMethod(\"" + getMethodName + "\", (Class<?>[])null)");
				} else {
					list.add(position, entityName + ".class.getMethod(\"" + getMethodName + "\", (Class<?>[])null)");
				}
			}

			// indirect methods
			if (!indirectMethods.containsKey(uri) && propEl.hasAttribute("name")) {
				String propName = propEl.getAttribute("name").substring(0, 1).toLowerCase()
				+ propEl.getAttribute("name").substring(1);
				String propNameUCFirst = propName.substring(0, 1).toUpperCase() + propName.substring(1);

				String method = "  public Object get" + propNameUCFirst + "(){\n";
				method += "    return this.getPropertyValue(\"" + uri + "\", false, true);\n";
				method += "  }\n\n";

				indirectMethods.put(uri, method);
			}

		}

		if (!anno.equals("")) {
			anno = "@IndirectProperty({" + anno + "})";
		}

		return anno;
	}

	private String getUnstoredAnnotation(Element property, Map<String, String> unstoredMethods, String parentPropTypeImpl) {
		String anno = "";
		NodeList nodeList = property.getElementsByTagName("unstoredProperty");
		boolean bFirst = true;
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element propEl = (Element) nodeList.item(i);

			String uri = propEl.getAttribute("uri");

			if (bFirst) {
				anno = "\"" + uri + "\"";
				bFirst = false;
			} else
				anno += ",\"" + uri + "\"";

			// indirect methods
			if (propEl.hasAttribute("name")) {
				String propName = propEl.getAttribute("name").substring(0, 1).toLowerCase()
				+ propEl.getAttribute("name").substring(1);
				String propNameUCFirst = propName.substring(0, 1).toUpperCase() + propName.substring(1);

				String method = "  public " + parentPropTypeImpl + " get" + propNameUCFirst + "(){\n";
				method += "    return (" + parentPropTypeImpl + ") this.getPropertyValue(\"" + uri + "\", false, true);\n";
				method += "  }\n\n";

				unstoredMethods.put(uri, method);
			}
		}

		if (!anno.equals("")) {
			anno = "@UnstoredProperty({" + anno + "})";
		}

		return anno;
	}

	private String getPropertyAnnotation(String propType, String uri, String collection) {
		switch (getPropertyAnnotationType(propType, collection)) {
		case SIMPLE_URI_PROPERTY:
			return "@SimpleURIProperty(\"" + uri + "\")";
		case SIMPLE_LITERAL_PROPERTY:
			return "@SimpleLiteralProperty(\n" + "    value = \"" + uri + "\",\n" + "    datatypeType = \"" + "javaprimitive"
			+ "\",\n" + "    javaType = \"" + propType + "\"\n" + "  )";
		case SIMPLE_COMPLEX_PROPERTY:
			return "@ComplexProperty(\"" + uri + "\")";
		case SIMPLE_RESOURCE_PROPERTY:
			return "@ResourceProperty(\"" + uri + "\")";
		case BAG_URI_PROPERTY:
			return "@BagURIProperty(\"" + uri + "\")";
		case BAG_LITERAL_PROPERTY:
			return "@BagLiteralProperty(\n" + "    value = \"" + uri + "\",\n" + "    datatypeType = \"" + "javaprimitive"
			+ "\",\n" + "    javaType = \"" + propType + "\"\n" + "  )";
		case BAG_COMPLEX_PROPERTY:
			return "@BagComplexProperty(\"" + uri + "\")";
		case BAG_RESOURCE_PROPERTY:
			return "@BagResourceProperty(\"" + uri + "\")";
		}

		return "";
	}

	/**
	 * Returns the appropriate method name for returning a literals value.
	 * 
	 * @param property
	 * @return
	 */
	private String getLiteralGetMethod(Element property) {
		switch (getLiteralType(property)) {
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
		case LITERAL_TYPE_DATETIME:
			return "getString()";
		case LITERAL_TYPE_DATE:
			return "getString()";
		}

		return null;
	}

	private int getLiteralType(Element property) {
		String propType = property.getAttribute("type");
		return getLiteralType(propType);
	}

	private int getLiteralType(String propType) {
		propType = propType.toLowerCase();
		if (propType.equals("string"))
			return LITERAL_TYPE_STRING;
		if (propType.equals("int") || propType.equals("integer"))
			return LITERAL_TYPE_INTEGER;
		if (propType.equals("double"))
			return LITERAL_TYPE_DOUBLE;
		if (propType.equals("float"))
			return LITERAL_TYPE_FLOAT;
		if (propType.equals("boolean"))
			return LITERAL_TYPE_BOOLEAN;
		if (propType.equals("datetime"))
			return LITERAL_TYPE_DATETIME;
		if (propType.equals("date"))
			return LITERAL_TYPE_DATE;

		throw new IllegalArgumentException("Unknown literal type: " + propType);
	}

	private int getPropertyAnnotationType(Element property) {
		String propType = property.getAttribute("type");
		String collection = property.getAttribute("collection").toLowerCase();

		return getPropertyAnnotationType(propType, collection);
	}

	private int getPropertyAnnotationType(String propType, String collection) {
		if (collection != null) {
			if (collection.equals(""))
				collection = null;
			else
				collection = collection.toLowerCase();
		}
		
		if (propType.equals("Resource")) {
			if (collection == null)
				return SIMPLE_RESOURCE_PROPERTY;
			else if (collection.equals("bag"))
				return BAG_RESOURCE_PROPERTY;
		}

		if (entityNames.contains(propType) || propType.equals("GabotoEntity")) {
			if (collection == null)
				return SIMPLE_URI_PROPERTY;
			else if (collection.equals("bag"))
				return BAG_URI_PROPERTY;
		}

		// Check if this is a bean
		if (beanNames.contains(propType))
			if (collection == null)
				return SIMPLE_COMPLEX_PROPERTY;
			else if (collection.equals("bag"))
				return BAG_COMPLEX_PROPERTY;
		
		if (collection == null)
			return SIMPLE_LITERAL_PROPERTY;
		else if (collection.equals("bag"))
			return BAG_LITERAL_PROPERTY;

		throw new RuntimeException("No property annotation found for: " + propType);
	}


	private void generateLookup() { 
		// generate lookup class
		// FIXME This should perhaps be generated for each ontology and then aggregated

		String lookupClass = "package " + packageName + ";\n\n";
		lookupClass += "import java.util.Collection;\n" + 
		"import java.util.HashMap;\n" + 
		"import java.util.HashSet;\n" + 
		"import java.util.Map;\n" + 
		"import java.util.Set;\n" + 
		"\n"  + 
		"import net.sf.gaboto.node.GabotoEntity;\n" + 
		"import net.sf.gaboto.node.GabotoBean;\n" + 
		"import net.sf.gaboto.GabotoRuntimeException;\n" + 
		"\n" +
		"import net.sf.gaboto.OntologyLookup;\n"  +
		"\n" ;

		lookupClass += "\n\n";
		lookupClass +=     // class comment
			"/**\n"
			+ " * Gaboto generated ontology lookup utility.\n"
			+ " * @see " + this.getClass().getCanonicalName() + "\n"
			+ " */\n";
		lookupClass += "@SuppressWarnings(\"unchecked\")\n";
		lookupClass += "public class " + lookupClassName + " implements OntologyLookup {\n";

		lookupClass += "  private static Map<String,String> entityClassLookupNames;\n";
		lookupClass += "  private static Map<String,Class<? extends GabotoEntity>> entityClassLookupClass;\n";
		lookupClass += "  private static Map<String,Class<? extends GabotoBean>> beanClassLookupClass;\n";
		lookupClass += "  private static Map<Class<? extends GabotoEntity>, String> classToURILookup;\n";
		lookupClass += "  private static Collection<String> entityClassNames;\n";
		lookupClass += "  private static Set<String> entityTypes;\n\n";
		lookupClass += "  private static Map<String,String> nameToURILookup;\n";

		lookupClass += "  static{\n";
		lookupClass += "    entityClassLookupNames = new HashMap<String,String>();\n\n";
		for (Entry<String, String> entry : entityClassLookup.entrySet()) {
			lookupClass += "    entityClassLookupNames.put(\"" + entry.getKey() + "\", \"" + entry.getValue() + "\");\n";
		}
		lookupClass += "  }\n\n";

		lookupClass += "  static{\n";
		lookupClass += "    entityClassLookupClass = new HashMap<String,Class<? extends GabotoEntity>>();\n\n";
		lookupClass += "    try {\n";
		for (Entry<String, String> entry : entityClassLookup.entrySet()) {
			lookupClass += "      entityClassLookupClass.put(\"" + entry.getKey()
			+ "\", (Class<?  extends GabotoEntity>) Class.forName(\"" + entitiesPackageName + "." + entry.getValue()
			+ "\"));\n";
		}
		lookupClass += "    } catch (ClassNotFoundException e) {\n";
		lookupClass += "      throw new GabotoRuntimeException(e);\n";
		lookupClass += "    }\n";
		lookupClass += "  }\n\n";

		lookupClass += "  static{\n";
		lookupClass += "    beanClassLookupClass = new HashMap<String,Class<? extends GabotoBean>>();\n\n";
		lookupClass += "    try {\n";
		for (Entry<String, String> entry : beanClassLookup.entrySet()) {
			lookupClass += "      beanClassLookupClass.put(\"" + entry.getKey()
			+ "\", (Class<?  extends GabotoBean>) Class.forName(\"" + beansPackageName + "." + entry.getValue()
			+ "\"));\n";
		}
		lookupClass += "    } catch (ClassNotFoundException e) {\n";
		lookupClass += "      throw new GabotoRuntimeException(e);\n";
		lookupClass += "    }\n";
		lookupClass += "  }\n\n";

		lookupClass += "  static{\n";
		lookupClass += "    classToURILookup = new HashMap<Class<? extends GabotoEntity>, String>();\n\n";
		lookupClass += "    try {\n";
		for (Entry<String, String> entry : entityClassLookup.entrySet()) {
			lookupClass += "      classToURILookup.put((Class<?  extends GabotoEntity>) Class.forName(\"" + entitiesPackageName
			+ "." + entry.getValue() + "\"), \"" + entry.getKey() + "\");\n";
		}
		lookupClass += "    } catch (ClassNotFoundException e) {\n";
		lookupClass += "      throw new GabotoRuntimeException(e);\n";
		lookupClass += "    }\n";
		lookupClass += "  }\n\n";

		lookupClass += "  static {\n";
		lookupClass += "    nameToURILookup = new HashMap<String,String>();\n";
		for (Entry<String, String> entry : entityClassLookup.entrySet()) {
			lookupClass += "    nameToURILookup.put(\"" + entry.getValue() + "\", \"" + entry.getKey() + "\");\n";
		}
		lookupClass += "  }\n\n";


		lookupClass += "  static{\n";
		lookupClass += "    entityTypes = new HashSet<String>();\n\n";
		for (String type : entityTypes) {
			lookupClass += "    entityTypes.add(\"" + type + "\");\n";
		}
		lookupClass += "  }\n\n";

		lookupClass += "  static{\n";
		lookupClass += "    entityClassNames = new HashSet<String>();\n\n";
		lookupClass += entityClassNames;
		lookupClass += "  }\n\n";

		lookupClass += "  public Set<String> getRegisteredClassesAsURIs(){\n";
		lookupClass += "    return entityTypes;\n";
		lookupClass += "  }\n\n";

		lookupClass += "  public Collection<String> getRegisteredEntityClassesAsClassNames(){\n";
		lookupClass += "    return entityClassNames;\n";
		lookupClass += "  }\n\n";

		lookupClass += "  public Class<? extends GabotoEntity> getEntityClassFor(String typeURI){\n";
		lookupClass += "    return entityClassLookupClass.get(typeURI);\n";
		lookupClass += "  }\n\n";

		lookupClass += "  public Class<? extends GabotoBean> getBeanClassFor(String typeURI){\n";
		lookupClass += "    return beanClassLookupClass.get(typeURI);\n";
		lookupClass += "  }\n\n";

		lookupClass += "  public String getLocalName(String typeURI){\n";
		lookupClass += "    return entityClassLookupNames.get(typeURI);\n";
		lookupClass += "  }\n\n";

		lookupClass += "  public boolean isValidName(String name) {\n";
		lookupClass += "    return entityClassNames.contains(name);\n";
		lookupClass += "  }\n\n";

		lookupClass += "  public String getTypeURIForEntityClass(Class<? extends GabotoEntity> clazz){\n";
		lookupClass += "    return classToURILookup.get(clazz);\n";
		lookupClass += "  }\n\n";

		lookupClass += "  public String getURIForName(String name){\n";
		lookupClass += "    return nameToURILookup.get(name);\n";
		lookupClass += "  }\n\n";

		lookupClass += "}\n";

		// write file
		try {
			File outputFile = new File(outputDir.getAbsolutePath() + File.separator + lookupClassName + ".java" );
			System.out.println("Write java class to: " + outputFile.getAbsolutePath());
			BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
			out.write(lookupClass);
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}


	}
	
	private Collection<String> getBeanNames(Document doc) {
		Set<String> names = new HashSet<String>();
		getBeanNames(doc.getDocumentElement(), names);
		return names;
	}
	
	private void getBeanNames(Element elem, Set<String> names) {
		if (elem.getNodeName() == "GabotoBean")
			names.add(elem.getAttribute("name"));
		
		NodeList nodes = elem.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node instanceof Element)
				getBeanNames((Element) node, names);
		}
	}

	/**
	 * Main that can be called from Maven in examples/oxpoints directory or from eclipse.
	 * @param args
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		if (args.length == 3)
			generate(new File(args[0]), new File(args[1]), args[2]);
		else
			generate(new File("examples/oxpoints/src/main/conf/Gaboto.xml"), new File("examples/oxpoints/src/main/java"), "uk.ac.ox.oucs.oxpoints.gaboto");
	}

	public static void generate(File config, File outputDir, String packageName) throws ParserConfigurationException,
	SAXException, IOException {
		new GabotoGenerator(config, outputDir, packageName).run();
	}

	class JavaText { 
		TreeSet<String> imports = new TreeSet<String>();
		boolean addImport(String imp) { 
			return imports.add(imp);
		}

		String getImports() { 
			String importsString = "";
			String packageString= "";
			String thisPackageString= "";
			for (String imp : imports) { 
				thisPackageString = imp.substring(0, imp.lastIndexOf('.'));
				if (!thisPackageString.equals(packageString)) { 
					importsString += "\n";
					packageString = thisPackageString;
				}
				importsString += "import " + imp + ";\n";
			}
			return importsString;
		}
	}

	class PropertyJavaText { 
		String importDefinitions = "";
		String propertyDefinitions = "";
		String methodDefinitions = ""; 
		String loadMethod = "";

		String propName = "";
		String propNameUCFirst = "";
		String propType = "";
		String propTypeItem = "";
		String collection = "";
		String uri = "";

		String realPropTypeInterface = "";
		String realPropTypeImpl = "";

		String getMethodName = "";

		String propertyAnnotation = "";
		String indirectAnnotation = "";
		String unstoredAnnotation = "";

		String setMethodName = "";

		String addMethodName = "";
		String parameterName = "";


		/**
		 * @return the importDefinitions
		 */
		public String getImportDefinitions() {
			return importDefinitions;
		}
		/**
		 * @return the propertyDefinitionss
		 */
		public String getPropertyDefinitions() {
			return propertyDefinitions;
		}
		/**
		 * @return the methodDefinitions
		 */
		public String getMethodDefinitions() {
			return methodDefinitions;
		}
		/**
		 * @return the loadEntitySnippet
		 */
		public String getLoadMethod() {
			return loadMethod;
		}
	}
}
