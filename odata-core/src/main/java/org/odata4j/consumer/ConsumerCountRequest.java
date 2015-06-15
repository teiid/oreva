package org.odata4j.consumer;

import org.odata4j.core.OCountRequest;
import org.odata4j.core.ODataVersion;
import org.odata4j.exceptions.ODataProducerException;
import org.odata4j.format.FormatType;

/**
 * Count-request implementation.
 */
public class ConsumerCountRequest implements OCountRequest {

  private ODataClient client;
  private String baseUri;
  private String entitySetName;
  private Integer top;

  public ConsumerCountRequest(ODataClient client, String serviceRootUri) {
    this.client = client;
    this.baseUri = serviceRootUri;
  }

  public ConsumerCountRequest entitySetName(String entitySetName) {
    this.entitySetName = entitySetName;
    return this;
  }

  public ConsumerCountRequest top(int top) {
    this.top = Integer.valueOf(top);
    return this;
  }

  public int execute() throws ODataProducerException {
    ODataClientRequest request = getRequest();

    String valueString = client.requestBody(client.getFormatType(), request);
    return Integer.parseInt(valueString);
  }

  private ODataClientRequest getRequest() {
    String uri = baseUri;

    if (entitySetName != null) {
      uri = uri + entitySetName + "/";
    }

    uri = uri + "$count";

    if (top != null) {
      uri = uri + "?$top=" + top;
    }

    ODataClientRequest request = ODataClientRequest.get(uri);

    return request;
  }

  // the client should get the count and set it in the batch response
  @Override
  public Integer getResult(ODataVersion version, Object payload, FormatType formatType) {
    if (payload instanceof String) {
      String valueString = (String) payload;
      return Integer.parseInt(valueString);
    }

    throw new IllegalArgumentException("the pay load type is not string for entityCount request");
  }

  @Override
  public String formatRequest(FormatType formatType) {
    ODataClientRequest request = getRequest();
    return ConsumerBatchRequestHelper.formatSingleRequest(request, formatType);
  }

}
