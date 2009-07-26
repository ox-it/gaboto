package org.oucs.gaboto.model;

import java.util.Collection;
import java.util.Set;

import org.oucs.gaboto.node.GabotoEntity;

public interface GabotoOntologyLookup {

  public abstract Set<String> getRegisteredClassesAsURIs();

  public abstract Collection<String> getRegisteredEntityClassesAsClassNames();

  public abstract Class<? extends GabotoEntity> getEntityClassFor(String typeURI);

  public abstract String getLocalName(String typeURI);

  public abstract boolean isValidName(String name);

  public abstract String getTypeURIForEntityClass(
      Class<? extends GabotoEntity> clazz);

}