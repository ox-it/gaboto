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
package org.oucs.gaboto.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Method;
import org.oucs.gaboto.entities.utils.SimpleLiteralProperty;
import org.oucs.gaboto.entities.utils.SimpleURIProperty;
import org.oucs.gaboto.entities.utils.ComplexProperty;
import org.oucs.gaboto.entities.utils.BagURIProperty;
import org.oucs.gaboto.entities.utils.BagLiteralProperty;
import org.oucs.gaboto.entities.utils.BagComplexProperty;
import org.oucs.gaboto.entities.utils.IndirectProperty;
import org.oucs.gaboto.entities.utils.UnstoredProperty;
import org.oucs.gaboto.entities.utils.PassiveProperty;
import org.oucs.gaboto.entities.utils.StaticProperty;
import org.oucs.gaboto.vocabulary.*;
import org.oucs.gaboto.entities.GabotoEntity;
import org.oucs.gaboto.entities.pool.GabotoEntityPool;
import org.oucs.gaboto.entities.pool.EntityExistsCallback;
import org.oucs.gaboto.entities.pool.PassiveEntitiesRequest;
import org.oucs.gaboto.model.GabotoSnapshot;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.ontology.OntClass;


@SuppressWarnings("unchecked")
public class GabotoOntologyLookup{
	private static Map<String,String> entityClassLookupNames;
	private static Map<String,Class<? extends GabotoEntity>> entityClassLookupClass;
	private static Map<Class<? extends GabotoEntity>, String> classToURILookup;
	private static Collection<String> entityClassNames;
	private static Set<String> entityTypes;

	static{
		entityClassLookupNames = new HashMap<String,String>();

		entityClassLookupNames.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Museum", "Museum");
		entityClassLookupNames.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Group", "Group");
		entityClassLookupNames.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#DrainCover", "DrainCover");
		entityClassLookupNames.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Carpark", "Carpark");
		entityClassLookupNames.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#ServiceDepartment", "ServiceDepartment");
		entityClassLookupNames.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Faculty", "Faculty");
		entityClassLookupNames.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Place", "Place");
		entityClassLookupNames.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Unit", "Unit");
		entityClassLookupNames.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#WAP", "WAP");
		entityClassLookupNames.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Site", "Site");
		entityClassLookupNames.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Entrance", "Entrance");
		entityClassLookupNames.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Library", "Library");
		entityClassLookupNames.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Division", "Division");
		entityClassLookupNames.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Building", "Building");
		entityClassLookupNames.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#OxpEntity", "OxpEntity");
		entityClassLookupNames.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#College", "College");
		entityClassLookupNames.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Image", "Image");
		entityClassLookupNames.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Department", "Department");
		entityClassLookupNames.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Website", "Website");
		entityClassLookupNames.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Room", "Room");
	}

	static{
		entityClassLookupClass = new HashMap<String,Class<? extends GabotoEntity>>();

		try {
			entityClassLookupClass.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Museum", (Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Museum"));
			entityClassLookupClass.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Group", (Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Group"));
			entityClassLookupClass.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#DrainCover", (Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.DrainCover"));
			entityClassLookupClass.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Carpark", (Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Carpark"));
			entityClassLookupClass.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#ServiceDepartment", (Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.ServiceDepartment"));
			entityClassLookupClass.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Faculty", (Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Faculty"));
			entityClassLookupClass.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Place", (Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Place"));
			entityClassLookupClass.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Unit", (Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Unit"));
			entityClassLookupClass.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#WAP", (Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.WAP"));
			entityClassLookupClass.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Site", (Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Site"));
			entityClassLookupClass.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Entrance", (Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Entrance"));
			entityClassLookupClass.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Library", (Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Library"));
			entityClassLookupClass.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Division", (Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Division"));
			entityClassLookupClass.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Building", (Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Building"));
			entityClassLookupClass.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#OxpEntity", (Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.OxpEntity"));
			entityClassLookupClass.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#College", (Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.College"));
			entityClassLookupClass.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Image", (Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Image"));
			entityClassLookupClass.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Department", (Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Department"));
			entityClassLookupClass.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Website", (Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Website"));
			entityClassLookupClass.put("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Room", (Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Room"));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	static{
		classToURILookup = new HashMap<Class<? extends GabotoEntity>, String>();

		try {
			classToURILookup.put((Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Museum"), "http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Museum");
			classToURILookup.put((Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Group"), "http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Group");
			classToURILookup.put((Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.DrainCover"), "http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#DrainCover");
			classToURILookup.put((Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Carpark"), "http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Carpark");
			classToURILookup.put((Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.ServiceDepartment"), "http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#ServiceDepartment");
			classToURILookup.put((Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Faculty"), "http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Faculty");
			classToURILookup.put((Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Place"), "http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Place");
			classToURILookup.put((Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Unit"), "http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Unit");
			classToURILookup.put((Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.WAP"), "http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#WAP");
			classToURILookup.put((Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Site"), "http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Site");
			classToURILookup.put((Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Entrance"), "http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Entrance");
			classToURILookup.put((Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Library"), "http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Library");
			classToURILookup.put((Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Division"), "http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Division");
			classToURILookup.put((Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Building"), "http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Building");
			classToURILookup.put((Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.OxpEntity"), "http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#OxpEntity");
			classToURILookup.put((Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.College"), "http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#College");
			classToURILookup.put((Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Image"), "http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Image");
			classToURILookup.put((Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Department"), "http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Department");
			classToURILookup.put((Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Website"), "http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Website");
			classToURILookup.put((Class<?  extends GabotoEntity>) Class.forName("org.oucs.gaboto.entities.Room"), "http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Room");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	static{
		entityTypes = new HashSet<String>();

		entityTypes.add("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Museum");
		entityTypes.add("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Group");
		entityTypes.add("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#DrainCover");
		entityTypes.add("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Carpark");
		entityTypes.add("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#ServiceDepartment");
		entityTypes.add("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Faculty");
		entityTypes.add("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Place");
		entityTypes.add("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Unit");
		entityTypes.add("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#WAP");
		entityTypes.add("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Site");
		entityTypes.add("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Entrance");
		entityTypes.add("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Library");
		entityTypes.add("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Division");
		entityTypes.add("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Building");
		entityTypes.add("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#OxpEntity");
		entityTypes.add("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#College");
		entityTypes.add("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Image");
		entityTypes.add("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Department");
		entityTypes.add("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Website");
		entityTypes.add("http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Room");
	}

	static{
		entityClassNames = new HashSet<String>();

		entityClassNames.add("OxpEntity");
		entityClassNames.add("Image");
		entityClassNames.add("Place");
		entityClassNames.add("Building");
		entityClassNames.add("Carpark");
		entityClassNames.add("DrainCover");
		entityClassNames.add("Entrance");
		entityClassNames.add("Room");
		entityClassNames.add("Site");
		entityClassNames.add("WAP");
		entityClassNames.add("Unit");
		entityClassNames.add("College");
		entityClassNames.add("Department");
		entityClassNames.add("Division");
		entityClassNames.add("Faculty");
		entityClassNames.add("Group");
		entityClassNames.add("Library");
		entityClassNames.add("Museum");
		entityClassNames.add("ServiceDepartment");
		entityClassNames.add("Website");
	}

	public static Set<String> getRegisteredClassesAsURIs(){
		return entityTypes;
	}

	public static Collection<String> getRegisteredEntityClassesAsClassNames(){
		return entityClassNames;
	}

	public static Class<? extends GabotoEntity> getEntityClassFor(String typeURI){
		return entityClassLookupClass.get(typeURI);
	}

	public static String getLocalName(String typeURI){
		return entityClassLookupNames.get(typeURI);
	}

	public static String getTypeURIForEntityClass(Class<? extends GabotoEntity> clazz){
		return classToURILookup.get(clazz);
	}

}
