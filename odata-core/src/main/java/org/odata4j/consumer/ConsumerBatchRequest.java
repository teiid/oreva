package org.odata4j.consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.odata4j.core.Guid;
import org.odata4j.core.OBatchRequest;
import org.odata4j.core.OBatchSupport;
import org.odata4j.core.OChangeSetRequest;
import org.odata4j.core.OCountRequest;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntityGetRequest;
import org.odata4j.core.OQueryRequest;
import org.odata4j.exceptions.ODataProducerException;
import org.odata4j.format.FormatType;
import org.odata4j.producer.resources.ODataBatchProvider;

/**
 * Implement batch request.
 * Copyright 2013 Halliburton
 * @author <a href="mailto:peng.chen@halliburton.com">Kevin Chen</a>
 *
 */
public class ConsumerBatchRequest implements OBatchRequest {
  // the list will hold 2 types request, ODataClientRequest or OChangeSetRequest
  List<OBatchSupport> reqs = new ArrayList<OBatchSupport>();
  final String rootUri;
  final ODataClient oClient;
  static final String batchEndPoint = "$batch";

  public ConsumerBatchRequest(ODataClient client, String serviceRootUri) {
    rootUri = serviceRootUri;
    oClient = client;
  }

  @Override
  public OBatchRequest addRequest(OEntityGetRequest<?> request) {
    reqs.add(request);
    return this;
  }

  @Override
  public OBatchRequest addRequest(OQueryRequest<?> request) {
    reqs.add(request);
    return this;
  }

  @Override
  public OBatchRequest addRequest(OCountRequest countRequest) {
    reqs.add(countRequest);
    return this;
  }

  @Override
  public OBatchRequest addRequest(OChangeSetRequest changeSetRequest) {
    reqs.add(changeSetRequest);
    return this;
  }

  @Override
  public List<ODataClientBatchResponse> execute() throws ODataProducerException {
    String boundary = "batch_" + Guid.randomGuid().toString();
    Map<String, String> batchHeaders = createBatchHeaders(boundary);
    String url = rootUri + batchEndPoint;

    ODataClientRequest batchReq = new ODataClientRequest("POST", url, batchHeaders, null, null);
    List<ODataClientBatchResponse> batchRes = oClient.batchRequest(FormatType.ATOM, batchReq, reqs);

    return batchRes;
  }

  private Map<String, String> createBatchHeaders(String boundary) {
    Map<String, String> headers = new HashMap<String, String>();
    String cType = ODataBatchProvider.MULTIPART_MIXED + "; " + "boundary=" + boundary;
    headers.put(ODataConstants.Headers.CONTENT_TYPE, cType);

    return headers;
  }
}
