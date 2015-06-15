package org.odata4j.consumer;

import org.core4j.Enumerable;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OModifyLinkRequest;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.exceptions.ODataProducerException;
import org.odata4j.format.FormatType;

/**
 * Update-link-request implementation.
 */
public class ConsumerUpdateLinkRequest extends AbstractConsumerEntityRequest<Void> implements OModifyLinkRequest {

  private final String targetNavProp;
  private final Object[] oldTargetKeyValues;
  private final OEntityId newTargetEntity;

  public ConsumerUpdateLinkRequest(ODataClient client, String serviceRootUri,
      EdmDataServices metadata, OEntityId sourceEntity, OEntityId newTargetEntity, String targetNavProp, Object... oldTargetKeyValues) {
    super(client, serviceRootUri, metadata, sourceEntity.getEntitySetName(), sourceEntity.getEntityKey());
    this.targetNavProp = targetNavProp;
    this.oldTargetKeyValues = oldTargetKeyValues;
    this.newTargetEntity = newTargetEntity;
  }

  @Override
  public Void execute() throws ODataProducerException {
    ODataClientRequest request = getRequest();
    getClient().updateLink(request);
    return null;
  }

  private ODataClientRequest getRequest() {
    String path = Enumerable.create(getSegments()).join("/");
    path = ConsumerQueryLinksRequest.linksPath(targetNavProp, oldTargetKeyValues).apply(path);

    ODataClientRequest request = ODataClientRequest.put(getServiceRootUri() + path, toSingleLink(newTargetEntity));

    return request;
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