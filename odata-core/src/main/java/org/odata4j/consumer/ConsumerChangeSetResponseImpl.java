package org.odata4j.consumer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.odata4j.core.ODataClientChangeSetResponse;

/**
 * An implementation of ODataClientChangeSetResponse.
 * 
 * Copyright 2013 Halliburton
 * @author <a href="mailto:peng.chen@halliburton.com">Kevin Chen</a>
 *
 */
public class ConsumerChangeSetResponseImpl implements ODataClientChangeSetResponse {
  List<ODataClientBatchResponse> results = new ArrayList<ODataClientBatchResponse>();
  int status = 200;

  @Override
  public int getStatus() {
    return status;
  }

  @Override
  public List<ODataClientBatchResponse> getEntity() {
    return results;
  }

  @Override
  public MultivaluedMap<String, String> getHeaders() {
    return null;
  }

  @Override
  public void close() {

  }

  @Override
  public void add(ODataClientBatchResponse result) {
    results.add(result);
  }

  @Override
  public InputStream getEntityInputStream() {
    throw new UnsupportedOperationException("not supported by ConsumerChangeSetResponseImpl");
  }

  @Override
  public MediaType getMediaType() {
    throw new UnsupportedOperationException("not supported by ConsumerChangeSetResponseImpl");
  }
}
