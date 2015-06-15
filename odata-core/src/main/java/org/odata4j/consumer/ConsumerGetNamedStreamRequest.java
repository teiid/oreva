package org.odata4j.consumer;

import java.io.InputStream;

import javax.ws.rs.core.Response.StatusType;

import org.odata4j.core.OEntity;
import org.odata4j.core.OErrors;
import org.odata4j.core.OGetNamedStreamRequest;
import org.odata4j.core.ONamedStreamLink;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.exceptions.BadRequestException;
import org.odata4j.exceptions.ODataProducerException;
import org.odata4j.internal.InternalUtil;

public class ConsumerGetNamedStreamRequest  implements OGetNamedStreamRequest {
  private ODataClient client;
  private String uri;
  private EdmDataServices metadata;
  private OEntity entity;
  private String resoruceName;

  public ConsumerGetNamedStreamRequest(ODataClient client, String serviceRootUri, EdmDataServices metadata, OEntity entity, String name) {
    this.client = client;
    this.uri = serviceRootUri;
    this.metadata = metadata;
    this.entity = entity;
    this.resoruceName = name;
  }

  @Override
  public InputStream execute() throws ODataProducerException {
    ONamedStreamLink link = null;    
    try {
      link = entity.getLink(resoruceName, ONamedStreamLink.class);
    } catch (IllegalArgumentException e) {
      throw new BadRequestException(String.format("the entity: %1s does not contain requested named resrouce stream: %2s", InternalUtil.getEntityRelId(entity), resoruceName));
    }
    
    ODataClientRequest request = ODataClientRequest.get(uri+link.getHref());
    ODataClientResponse responseStream = client.getEntity(request); 
    
    return responseStream == null ? null : responseStream.getEntityInputStream();
  }
}
