package org.odata4j.consumer;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.core4j.Enumerable;
import org.core4j.Predicate1;
import org.odata4j.core.ODataConstants.Headers;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OModifyRequest;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.exceptions.ODataProducerException;
import org.odata4j.format.Entry;
import org.odata4j.format.FormatType;
import org.odata4j.internal.EntitySegment;

/**
 * Modification-request implementation.
 */
public class ConsumerEntityModificationRequest<T> extends AbstractConsumerEntityPayloadRequest implements OModifyRequest<T> {

  private final T updateRoot;
  private final ODataClient client;

  private final List<EntitySegment> segments = new ArrayList<EntitySegment>();

  private EdmEntitySet entitySet;
  private String ifMatch;

  public ConsumerEntityModificationRequest(T updateRoot, ODataClient client, String serviceRootUri, EdmDataServices metadata,
      String entitySetName, OEntityKey key, String ifMatch) {
    super(entitySetName, serviceRootUri, metadata);
    this.updateRoot = updateRoot;
    this.client = client;
    this.ifMatch = ifMatch;

    segments.add(new EntitySegment(entitySetName, key));
    this.entitySet = metadata.getEdmEntitySet(entitySetName);
  }

  @Override
  public OModifyRequest<T> nav(String navProperty, OEntityKey key) {
    segments.add(new EntitySegment(navProperty, key));
    entitySet = metadata.getEdmEntitySet(entitySet.getType().findNavigationProperty(navProperty).getToRole().getType());
    return this;
  }

  @Override
  public void execute() throws ODataProducerException {

    ODataClientRequest request = null;
    if (updateRoot != null) {
      OEntity entity = (OEntity) updateRoot;
      // if mediaStream is set, then first send put request for updating only media link;
      if (Boolean.TRUE.equals(entitySet.getType().getHasStream()) && isMediaStreamSet(entity)) {
        String path = Enumerable.create(segments).join("/");
        request = new ODataClientRequest("PUT", serviceRootUri + path, null, null, entity.getMediaLinkStream());
        client.updateEntity(request);
      }
    }
    // send regular merge/put request for updating non-stream properties
    request = getRequest();

    client.updateEntity(request);
  }

  @Override
  public OModifyRequest<T> properties(OProperty<?>... props) {
    return super.properties(this, props);
  }

  @Override
  public OModifyRequest<T> properties(Iterable<OProperty<?>> props) {
    return super.properties(this, props);
  }

  @Override
  public OModifyRequest<T> link(String navProperty, OEntity target) {
    return super.link(this, navProperty, target);
  }

  @Override
  public OModifyRequest<T> link(String navProperty, OEntityKey targetKey) {
    return super.link(this, navProperty, targetKey);
  }

  @Override
  public OModifyRequest<T> ifMatch(String precondition) {
    ifMatch = precondition;
    return this;
  }

  private ODataClientRequest getRequest() {
    List<OProperty<?>> requestProps = props;
    if (updateRoot != null) {
      OEntity updateRootEntity = (OEntity) updateRoot;
      requestProps = Enumerable.create(updateRootEntity.getProperties()).toList();
      for (final OProperty<?> prop : props) {
        OProperty<?> requestProp = Enumerable.create(requestProps).firstOrNull(new Predicate1<OProperty<?>>() {
          public boolean apply(OProperty<?> input) {
            return input.getName().equals(prop.getName());
          }
        });
        requestProps.remove(requestProp);
        requestProps.add(prop);
      }
    }

    OEntityKey entityKey = Enumerable.create(segments).last().key;
    Entry entry = client.createRequestEntry(entitySet, entityKey, requestProps, links);

    String path = Enumerable.create(segments).join("/");

    ODataClientRequest request = updateRoot != null ?
        ODataClientRequest.put(serviceRootUri + path, entry) :
        ODataClientRequest.merge(serviceRootUri + path, entry);

    // if this is update request (w/o stream) then send it as merge
    if (Boolean.TRUE.equals(entitySet.getType().getHasStream())) {
      request = ODataClientRequest.merge(serviceRootUri + path, entry);
    }
    if (ifMatch != null)
      request.header(Headers.IF_MATCH, ifMatch);

    return request;
  }

  /**
   * Checks if media stream is set.
   * Iterates through Oproperties and set mediaLinkStream property in OEntity.
   * @param entity the entity
   * @return the boolean
   */
  private Boolean isMediaStreamSet(OEntity entity) {
    // unset MediaLink first 
    entity.setMediaLinkStream(null);
    // check whether the stream has been set using Oproperties
    for (OProperty<?> property : props) {
      if (property.getType().equals(EdmSimpleType.STREAM) && property.getValue() != null) {
        // media stream property is set, so set it in oentity.
    	  try {
			  if(property.getValue() instanceof  Blob) {
				  entity.setMediaLinkStream(((Blob) property.getValue()).getBinaryStream());
			  } else {
				  entity.setMediaLinkStream((InputStream) property.getValue());
			  }
    	  }catch (SQLException e) {
			  throw new RuntimeException(e);
		  }
        // remove it from props to prevent sending it.
        props.remove(property);
        return true;
      }
    }
    return false;
  }

  @Override
  public String formatRequest(FormatType formatType) {
    ODataClientRequest request = getRequest();
    return ConsumerBatchRequestHelper.formatSingleRequest(request, formatType);
  }

  @Override
  public Object getResult(ODataVersion version, Object payload, FormatType formatType) {
    return null;
  }

}
