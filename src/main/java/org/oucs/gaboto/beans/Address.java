package org.oucs.gaboto.beans;

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


public class Address extends GabotoBean {
	private String postCode;
	private String streetAddress;
	@Override
	public String getType(){
		return "http://nwalsh.com/rdf/vCard#Address";
	}
	@SimpleLiteralProperty(
		value = "http://nwalsh.com/rdf/vCard#postal_code",
		datatypeType = "javaprimitive",
		javaType = "String"
	)
	public String getPostCode(){
		return this.postCode;
	}

	@SimpleLiteralProperty(
		value = "http://nwalsh.com/rdf/vCard#postal_code",
		datatypeType = "javaprimitive",
		javaType = "String"
	)
	public void setPostCode(String postCode){
		this.postCode = postCode;
	}

	@SimpleLiteralProperty(
		value = "http://nwalsh.com/rdf/vCard#street_address",
		datatypeType = "javaprimitive",
		javaType = "String"
	)
	public String getStreetAddress(){
		return this.streetAddress;
	}

	@SimpleLiteralProperty(
		value = "http://nwalsh.com/rdf/vCard#street_address",
		datatypeType = "javaprimitive",
		javaType = "String"
	)
	public void setStreetAddress(String streetAddress){
		this.streetAddress = streetAddress;
	}





	public void loadFromResource(Resource res, GabotoSnapshot snapshot, GabotoEntityPool pool) {
		super.loadFromResource(res, snapshot, pool);
		Statement stmt;

		stmt = res.getProperty(snapshot.getProperty("http://nwalsh.com/rdf/vCard#postal_code"));
		if(null != stmt && stmt.getObject().isLiteral())
			this.setPostCode(((Literal)stmt.getObject()).getString());

		stmt = res.getProperty(snapshot.getProperty("http://nwalsh.com/rdf/vCard#street_address"));
		if(null != stmt && stmt.getObject().isLiteral())
			this.setStreetAddress(((Literal)stmt.getObject()).getString());

	}
}