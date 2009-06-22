package org.oucs.gaboto.beans;

import org.oucs.gaboto.entities.pool.GabotoEntityPool;
import org.oucs.gaboto.entities.utils.SimpleLiteralProperty;
import org.oucs.gaboto.model.GabotoSnapshot;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;


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
	
	public String toString() { 
    return this.streetAddress + ", " + this.postCode;
	}
}