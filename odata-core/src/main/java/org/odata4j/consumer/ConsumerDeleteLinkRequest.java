package org.odata4j.consumer;

import org.core4j.Enumerable;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OModifyLinkRequest;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.exceptions.ODataProducerException;
import org.odata4j.format.FormatType;

/**
 * Delete-link-request implementation.
 */
public class ConsumerDeleteLinkRequest extends AbstractConsumerEntityRequest<Void> implements OModifyLinkRequest {

  private final String targetNavProp;
  private final Object[] targetKeyValues;

  public ConsumerDeleteLinkRequest(ODataClient client, String serviceRootUri,
      EdmDataServices metadata, OEntityId sourceEntity, String targetNavProp, Object... targetKeyValues) {
    super(client, serviceRootUri, metadata, sourceEntity.getEntitySetName(), sourceEntity.getEntityKey());
    this.targetNavProp = targetNavProp;
    this.targetKeyValues = targetKeyValues;
  }

  @Override
  public Void execute() throws ODataProducerException {
    ODataClientRequest request = getRequest();
    getClient().deleteLink(request);
    return null;
  }

  private ODataClientRequest getRequest() {
    String path = Enumerable.create(getSegments()).join("/");
    path = ConsumerQueryLinksRequest.linksPath(targetNavProp, targetKeyValues).apply(path);
    ODataClientRequest request = ODataClientRequest.delete(getServiceRootUri() + path);

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
