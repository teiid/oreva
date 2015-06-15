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
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityIds;
import org.odata4j.core.OEntityKey;
import org.odata4j.producer.ODataProducer;

/**
 * 
 * Copyright 2013 Halliburton
 * @author <a href="mailto:peng.chen@halliburton.com">Kevin Chen</a>
 *
 */
public class ODataBatchPostUnit extends ODataBatchSingleUnit {

  protected ODataBatchPostUnit(HttpHeaders hHeaders, UriInfo uriInfo, String uri, String contents, MultivaluedMap<String, String> headers) throws URISyntaxException {
    super(uriInfo, uri, contents, headers);
  }

  @Override
  protected Response delegate(HttpHeaders httpHeaders, URI baseUri, ContextResolver<ODataProducer> producerResolver) throws Exception {
    ODataProducer producer = producerResolver.getContext(ODataProducer.class);

    if (hasLinkProperty()) {
      // here is a create link request, pay load will be the target entity
      String entityKeyString = getEntityKey();
      OEntityKey entityKey = null;
      if (entityKeyString != null) {
        entityKey = OEntityKey.parse(entityKeyString);
      }

      OEntityId sourceEntity = OEntityIds.create(getEnitySetName(), entityKey);

      OEntityId newTargetEntity = parseLinkRequestUri(httpHeaders, getUriInfo(), getResourceContent());
      producer.createLink(null, sourceEntity, getLinkTargetProperty(), newTargetEntity);

      return Response.noContent().header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION_HEADER).build();

    } else {
      // here is create entity request, pay load will be the entity
      String entityKeyString = getEntityKey();
      OEntityKey entityKey = null;
      if (entityKeyString != null) {
        entityKey = OEntityKey.parse(entityKeyString);
      }

      OEntity entity = new BatchRequestResource().getRequestEntity(httpHeaders, getResourceHeaders(),
          getUriInfo(),
          getResourceContent(),
          producer.getMetadata(),
          getEnitySetName(),
          entityKey, false);
      Response response = new EntitiesRequestResource().createEntity(httpHeaders, getUriInfo(), null, producer, getEnitySetName(), entity, null, getMediaTypeListForBatch());
      return response;
    }
  }
}
