package org.oucs.gaboto.entities;

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
import org.oucs.gaboto.entities.Unit;


/**
 *<p>This class was automatically generated by Gaboto<p>
 */
public class Group extends Unit {


	private static Map<String, List<Method>> indirectPropertyLookupTable;
	static{
		indirectPropertyLookupTable = new HashMap<String, List<Method>>();
		List<Method> list;

		try{
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getType(){
		return "http://ns.ox.ac.uk/namespace/oxpoints/2009/02/owl#Group";
	}







	public List<Method> getIndirectMethodsForProperty(String propertyURI){
		List<Method> list = super.getIndirectMethodsForProperty(propertyURI);
		if(null == list)
			return indirectPropertyLookupTable.get(propertyURI);
		
		else{
			List<Method> tmp = indirectPropertyLookupTable.get(propertyURI);
			if(null != tmp)
				list.addAll(tmp);
		}
		return list;
	}

}