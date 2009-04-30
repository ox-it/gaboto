package org.oucs.gaboto.beans;

import org.oucs.gaboto.entities.pool.GabotoEntityPool;
import org.oucs.gaboto.entities.utils.SimpleLiteralProperty;
import org.oucs.gaboto.model.GabotoSnapshot;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class Location extends GabotoBean {
  private String pos;

  @Override
  public String getType() {
    return "http://www.opengis.net/gml/Point";
  }

  @SimpleLiteralProperty(value = "http://www.opengis.net/gml/pos", datatypeType = "javaprimitive", javaType = "String")
  public String getPos() {
    return this.pos;
  }

  @SimpleLiteralProperty(value = "http://www.opengis.net/gml/pos", datatypeType = "javaprimitive", javaType = "String")
  public void setPos(String pos) {
    this.pos = pos;
  }

  public Double getLatitude() {
    try {
      return Double.valueOf(getPos().split(" ")[1]);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public Double getLongitude() {
    try {
      return Double.valueOf(getPos().split(" ")[0]);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public void loadFromResource(Resource res, GabotoSnapshot snapshot,
      GabotoEntityPool pool) {
    super.loadFromResource(res, snapshot, pool);
    Statement stmt;

    stmt = res.getProperty(snapshot
        .getProperty("http://www.opengis.net/gml/pos"));
    if (null != stmt && stmt.getObject().isLiteral())
      this.setPos(((Literal) stmt.getObject()).getString());

  }
}