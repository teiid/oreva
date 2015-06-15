package org.odata4j.consumer;

import java.io.InputStream;
import java.util.List;

import org.odata4j.core.OEntity;
import org.odata4j.core.OLink;
import org.odata4j.core.ONamedStreamLink;
import org.odata4j.core.OProperties;
import org.odata4j.core.OUpdateNamedStreamRequest;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.exceptions.BadRequestException;
import org.odata4j.exceptions.ODataProducerException;
import org.odata4j.internal.InternalUtil;

/**
 * an implementation of update named stream request interface.
 * @author <a href="mailto:kevin.chen@halliburton.com">Kevin Chen</a>
 *
 */
public class ConsumerUpdateNamedStreamRequest implements OUpdateNamedStreamRequest {
  final private ODataClient client;
  final private String uri;
  final private EdmDataServices metadata;
  final private OEntity entity;
  final private String resoruceName;
  final private InputStream is;

  public ConsumerUpdateNamedStreamRequest(ODataClient client, String serviceRootUri, EdmDataServices metadata, OEntity entity, String name, InputStream is) {
    this.client = client;
    this.uri = serviceRootUri;
    this.metadata = metadata;
    this.entity = entity;
    this.resoruceName = name;
    this.is = is;
  }

  private OLink findLInk() {
    List<OLink> links = entity.getLinks();
    String rel = "http://schemas.microsoft.com/ado/2007/08/dataservices/edit-media/"+resoruceName;
    
    for (OLink link : links) {
      if (link.getRelation().equals(rel)) {
        return link;
      }
    }
    return null;
  }
  
  @Override
  public void execute() throws ODataProducerException {
    OLink link = findLInk();
    
    if (link == null) {
      throw new BadRequestException(String.format("the entity: %1s does not contain updatable requested named resrouce stream: %2s", InternalUtil.getEntityRelId(entity), resoruceName));
    }

    ODataClientRequest request = new ODataClientRequest("PUT", uri + link.getHref(), null, null, is);
    client.updateEntity(request);
  }

}
