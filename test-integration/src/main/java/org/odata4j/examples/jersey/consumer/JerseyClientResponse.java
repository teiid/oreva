package org.odata4j.examples.jersey.consumer;

import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.odata4j.consumer.ODataClientResponse;

import com.sun.jersey.api.client.ClientResponse;

public class JerseyClientResponse implements ODataClientResponse {

  private ClientResponse clientResponse;

  public JerseyClientResponse(ClientResponse clientResponse) {
    this.clientResponse = clientResponse;
  }

  public ClientResponse getClientResponse() {
    return clientResponse;
  }

  @Override
  public MultivaluedMap<String, String> getHeaders() {
    return clientResponse.getHeaders();
  }

  @Override
  public void close() {}

  @Override
  public InputStream getEntityInputStream() {
    return clientResponse.getEntityInputStream();
  }

  @Override
  public MediaType getMediaType() {
    return clientResponse.getType();
  }
}
