package net.sf.gaboto;

import java.util.Collection;
import java.util.Set;

import net.sf.gaboto.node.GabotoEntity;

public interface OntologyLookup {

  public abstract Set<String> getRegisteredClassesAsURIs();

  public abstract Collection<String> getRegisteredEntityClassesAsClassNames();

  public abstract Class<? extends GabotoEntity> getEntityClassFor(String typeURI);

  public abstract String getLocalName(String typeURI);

  public abstract boolean isValidName(String name);

  public abstract String getTypeURIForEntityClass(
      Class<? extends GabotoEntity> clazz);

}