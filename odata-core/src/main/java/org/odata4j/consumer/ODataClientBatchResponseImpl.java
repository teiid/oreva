package org.odata4j.consumer;

import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

public class ODataClientBatchResponseImpl implements ODataClientBatchResponse {
  final Object entity;
  final int status;
  final MultivaluedMap<String, String> headers;

  public ODataClientBatchResponseImpl(int statusCode, MultivaluedMap<String, String> inHeaders, Object result) {
    status = statusCode;
    headers = inHeaders;
    entity = result;
  }

  @Override
  public MultivaluedMap<String, String> getHeaders() {
    return headers;
  }

  @Override
  public void close() {

  }

  @Override
  public int getStatus() {
    return status;
  }

  @Override
  public Object getEntity() {
    return entity;
  }

  @Override
  public InputStream getEntityInputStream() {
    throw new UnsupportedOperationException("not supported by ODataClientBatchResponse");
  }

  @Override
  public MediaType getMediaType() {
    throw new UnsupportedOperationException("not supported by ODataClientBatchResponse");
  }

}
