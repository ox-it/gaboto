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


public class Location extends GabotoBean {
	private String pos;
	@Override
	public String getType(){
		return "http://www.opengis.net/gml/Point";
	}
	@SimpleLiteralProperty(
		value = "http://www.opengis.net/gml/pos",
		datatypeType = "javaprimitive",
		javaType = "String"
	)
	public String getPos(){
		return this.pos;
	}

	@SimpleLiteralProperty(
		value = "http://www.opengis.net/gml/pos",
		datatypeType = "javaprimitive",
		javaType = "String"
	)
	public void setPos(String pos){
		this.pos = pos;
	}



 
                    
                    public Double getLatitude(){ 
                        try{ 
                            return Double.valueOf(getPos().split(" ")[1]); 
                        }catch(NumberFormatException e){ 
                            return null; 
                        } 
                     } 
                     
                 
                    
                    public Double getLongitude(){ 
                        try{ 
                            return Double.valueOf(getPos().split(" ")[0]); 
                        }catch(NumberFormatException e){ 
                            return null; 
                        } 
                    } 
                    
                

	public void loadFromResource(Resource res, GabotoSnapshot snapshot, GabotoEntityPool pool) {
		super.loadFromResource(res, snapshot, pool);
		Statement stmt;

		stmt = res.getProperty(snapshot.getProperty("http://www.opengis.net/gml/pos"));
		if(null != stmt && stmt.getObject().isLiteral())
			this.setPos(((Literal)stmt.getObject()).getString());

	}
}