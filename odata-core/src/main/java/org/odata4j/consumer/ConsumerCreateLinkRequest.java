package org.odata4j.consumer;

import org.core4j.Enumerable;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OModifyLinkRequest;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.exceptions.ODataProducerException;
import org.odata4j.format.FormatType;

/**
 * Create-link-request implementation.
 */
public class ConsumerCreateLinkRequest extends AbstractConsumerEntityRequest<Void> implements OModifyLinkRequest {

  private final String targetNavProp;
  private final OEntityId targetEntity;

  public ConsumerCreateLinkRequest(ODataClient client, String serviceRootUri,
      EdmDataServices metadata, OEntityId sourceEntity, String targetNavProp, OEntityId targetEntity) {
    super(client, serviceRootUri, metadata, sourceEntity.getEntitySetName(), sourceEntity.getEntityKey());
    this.targetNavProp = targetNavProp;
    this.targetEntity = targetEntity;
  }

  @Override
  public Void execute() throws ODataProducerException {
    ODataClientRequest request = getRequest();
    getClient().createLink(request);
    return null;
  }

  private ODataClientRequest getRequest() {
    String path = Enumerable.create(getSegments()).join("/");
    path = ConsumerQueryLinksRequest.linksPath(targetNavProp, null).apply(path);

    ODataClientRequest request = ODataClientRequest.post(getServiceRootUri() + path, toSingleLink(targetEntity));

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
