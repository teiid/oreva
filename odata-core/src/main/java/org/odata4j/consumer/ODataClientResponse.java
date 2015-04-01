package org.odata4j.consumer;

import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Generic OData http response used by the low-level {@link ODataClient} api.
 *
 * @see ODataClient
 */
public interface ODataClientResponse {

  MultivaluedMap<String, String> getHeaders();

  InputStream getEntityInputStream();
  MediaType getMediaType();

  void close();
}
