package org.odata4j.consumer;

import java.util.ArrayList;
import java.util.List;

import org.odata4j.core.Guid;
import org.odata4j.core.OBatchSupport;
import org.odata4j.core.OChangeSetRequest;
import org.odata4j.core.OCreateRequest;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OEntityDeleteRequest;
import org.odata4j.core.OModifyLinkRequest;
import org.odata4j.core.OModifyRequest;
import org.odata4j.format.FormatType;
import org.odata4j.producer.resources.ODataBatchProvider;

/**
 * An implementation of the OChangeSetRequest
 *
 * Copyright 2013 Halliburton
 * @author <a href="mailto:peng.chen@halliburton.com">Kevin Chen</a>
 */
public class ConsumerChangeSetRequest implements OChangeSetRequest {
  final ODataClient client;
  List<OBatchSupport> reqs = new ArrayList<OBatchSupport>();

  public ConsumerChangeSetRequest(ODataClient oClient) {
    client = oClient;
  }

  @Override
  public List<OBatchSupport> getReqs() {
    return reqs;
  }

  @Override
  public String formatRequest(FormatType formatType) {
    StringBuilder sb = new StringBuilder();

    // nothing to add
    if (reqs == null || reqs.size() == 0) {
      return "";
    }

    String boundary = "changeset_" + Guid.randomGuid().toString();
    String cType = ODataBatchProvider.MULTIPART_MIXED + "; " + "boundary=" + boundary;
    sb.append(ODataConstants.Headers.CONTENT_TYPE).append(": ").append(cType).append("\n");
    sb.append("\n");

    for (OBatchSupport req : reqs) {
      sb.append("\n--").append(boundary).append("\n");
      sb.append(req.formatRequest(formatType));
    }

    // ending the change set
    sb.append("\n--").append(boundary).append("--\n");

    return sb.toString();
  }

  @Override
  public Object getResult(ODataVersion version, Object payLoad, FormatType formatType) {
    // we send the a single response in case of changeset failure; so return the detailed error message which was part of the response.
    return payLoad.toString();
  }

  @Override
  public OChangeSetRequest addRequest(OCreateRequest<?> request) {
    reqs.add(request);
    return this;
  }

  @Override
  public OChangeSetRequest addRequest(OModifyRequest<?> request) {
    reqs.add(request);
    return this;
  }

  @Override
  public OChangeSetRequest addRequest(OEntityDeleteRequest request) {
    reqs.add(request);
    return this;
  }

  @Override
  public OChangeSetRequest addRequest(OModifyLinkRequest request) {
    reqs.add(request);
    return this;
  }

}
