package org.odata4j.consumer;

import org.core4j.Enumerable;
import org.odata4j.core.ODataConstants.Headers;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OEntityDeleteRequest;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.exceptions.ODataProducerException;
import org.odata4j.format.FormatType;

/**
 * Delete-request implementation.
 */
public class ConsumerDeleteEntityRequest extends AbstractConsumerEntityRequest<Void> implements OEntityDeleteRequest {

  private String ifMatch;

  public ConsumerDeleteEntityRequest(ODataClient client, String serviceRootUri,
      EdmDataServices metadata, String entitySetName, OEntityKey key, String ifMatch) {
    super(client, serviceRootUri, metadata, entitySetName, key);
    this.ifMatch = ifMatch;
  }

  @Override
  public Void execute() throws ODataProducerException {
    ODataClientRequest request = getRequest();
    getClient().deleteEntity(request);
    return null;
  }

  @Override
  public OEntityDeleteRequest ifMatch(String precondition) {
    ifMatch = precondition;
    return this;
  }

  private ODataClientRequest getRequest() {
    String path = Enumerable.create(getSegments()).join("/");
    ODataClientRequest request = ODataClientRequest.delete(getServiceRootUri() + path);
    if (ifMatch != null)
      request.header(Headers.IF_MATCH, ifMatch);

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
