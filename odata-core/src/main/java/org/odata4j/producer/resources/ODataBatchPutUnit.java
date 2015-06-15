package org.odata4j.producer.resources;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;

import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.producer.ODataProducer;

/**
 * 
 * Copyright 2013 Halliburton
 * @author <a href="mailto:peng.chen@halliburton.com">Kevin Chen</a>
 *
 */
public class ODataBatchPutUnit extends ODataBatchSingleUnit {

  protected ODataBatchPutUnit(HttpHeaders hHeaders, UriInfo uriInfo, String uri, String contents, MultivaluedMap<String, String> headers) throws URISyntaxException {
    super(uriInfo, uri, contents, headers);
  }

  @Override
  protected Response delegate(HttpHeaders httpHeaders, URI baseUri, ContextResolver<ODataProducer> producerResolver) throws Exception {

    ODataProducer producer = producerResolver.getContext(ODataProducer.class);
    OEntity entity = new BatchRequestResource().getRequestEntity(httpHeaders, getResourceHeaders(),
        getUriInfo(),
        getResourceContent(),
        producer.getMetadata(),
        getEnitySetName(),
        OEntityKey.parse(getEntityKey()), false);
    producer.updateEntity(null, getEnitySetName(), entity);
    Response response = Response.ok().header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION_HEADER)
        .header(ODataConstants.Headers.CONTENT_TYPE, getResourceHeaders().getFirst(ODataConstants.Headers.CONTENT_TYPE))
        .header(ODataConstants.Headers.CONTENT_LENGTH, 0).build();

    return response;
  }

}
