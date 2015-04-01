package org.odata4j.format;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityIds;

public class SingleLinks implements Iterable<SingleLink> {

  private final List<SingleLink> links;
  private final String targetNavProp;
  private final OEntityId sourceEntity;

  private SingleLinks(Collection<SingleLink> links) {
    this.links = new ArrayList<SingleLink>(links);
    this.targetNavProp = null;
    this.sourceEntity = null;
  }

  private SingleLinks(Collection<SingleLink> links, OEntityId sourceEntity, String targetNavProp) {
    this.links = new ArrayList<SingleLink>(links);
    this.targetNavProp = targetNavProp;
    this.sourceEntity = sourceEntity;
  }

  @Override
  public Iterator<SingleLink> iterator() {
    return links.iterator();
  }

  public static SingleLink create(String uri) {
    return new SingleLinkImpl(uri);
  }

  public static SingleLinks create(String serviceRootUri, Iterable<OEntityId> entities) {
    List<SingleLink> rt = new ArrayList<SingleLink>();
    for (OEntityId e : entities)
      rt.add(create(serviceRootUri, e));
    return new SingleLinks(rt);
  }

  public static SingleLink create(String serviceRootUri, OEntityId entity) {
    String uri = serviceRootUri;
    if (!uri.endsWith("/"))
      uri += "/";
    uri += OEntityIds.toKeyString(entity);
    return create(uri);
  }

  public static SingleLink create(String serviceRootUri, OEntityId entity, OEntityId sourceEntity, String targetNavProp) {
    String uri = serviceRootUri;
    if (!uri.endsWith("/"))
      uri += "/";
    uri += OEntityIds.toKeyString(entity);
    return new SingleLinkImpl(uri, sourceEntity, targetNavProp);
  }

  public static SingleLinks create(String serviceRootUri, Collection<OEntityId> entities, OEntityId sourceEntity, String targetNavProp) {
    List<SingleLink> rt = new ArrayList<SingleLink>();
    for (OEntityId e : entities)
      rt.add(create(serviceRootUri, e));
    return new SingleLinks(rt, sourceEntity, targetNavProp);
  }

  public String getTargetNavProp() {
    return targetNavProp;
  }

  public OEntityId getSourceEntity() {
    return sourceEntity;
  }

  private static class SingleLinkImpl implements SingleLink {

    private final String uri;
    private final String targetNavProp;
    private final OEntityId sourceEntity;

    public SingleLinkImpl(String uri) {
      this.uri = uri;
      this.sourceEntity = null;
      this.targetNavProp = null;
    }

    public SingleLinkImpl(String uri, OEntityId sourceEntity, String targetNavProp) {
      this.uri = uri;
      this.sourceEntity = sourceEntity;
      this.targetNavProp = targetNavProp;
    }

    @Override
    public String getUri() {
      return uri;
    }

    @Override
    public String toString() {
      return String.format("SingleLink[%s]", uri);
    }

    @Override
    public String getTargetNavProp() {
      return targetNavProp;
    }

    @Override
    public OEntityId getSourceEntity() {
      return sourceEntity;
    }
  }

}
