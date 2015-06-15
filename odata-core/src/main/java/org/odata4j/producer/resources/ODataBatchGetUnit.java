package org.odata4j.producer.resources;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;

import org.odata4j.core.ODataBatchUriInfo;
import org.odata4j.producer.ODataProducer;

/**
 * 
 * Copyright 2013 Halliburton
 * @author <a href="mailto:peng.chen@halliburton.com">Kevin Chen</a>
 *
 */
public class ODataBatchGetUnit extends ODataBatchSingleUnit {

	protected ODataBatchGetUnit(HttpHeaders hHeaders, UriInfo uriInfo,
			String uri, MultivaluedMap<String, String> headers)
			throws URISyntaxException {
		super(uriInfo, uri, null, headers);
	}

	@Override
	protected Response delegate(HttpHeaders httpHeaders, URI baseUri,
			ContextResolver<ODataProducer> producerResolver) throws Exception {
		Response response = null;
		if (getEntityKey() != null) {
			if (hasLinkProperty()) {
				// handle link property
				LinksRequestResource lrr = getLinkRequestResouce();

				 response = lrr.getLinks(httpHeaders, getUriInfo(), producerResolver, 
						 null, getQueryStringsMap().getFirst("$format"), getQueryStringsMap().getFirst("$callback"));
				 
			} else if (getNavProperty() != null) {
				// handle navigation property
				response = new PropertyRequestResource().getNavProperty(httpHeaders, getUriInfo(), producerResolver, null, getEnitySetName(), getEntityKey(), getNavProperty(), 
						getQueryStringsMap().getFirst("$inlineCount"), 
						getQueryStringsMap().getFirst("$top"), 
						getQueryStringsMap().getFirst("$skip"),
						getQueryStringsMap().getFirst("$filter"),
						getQueryStringsMap().getFirst("$orderBy"),
						getQueryStringsMap().getFirst("$format"),
						getQueryStringsMap().getFirst("$callback"),
						getQueryStringsMap().getFirst("$skipToken"),
						getQueryStringsMap().getFirst("$expand"),
						getQueryStringsMap().getFirst("$select"));

			} else {
				// regular entity request
				response = new EntityRequestResource().getEntity(httpHeaders,
						getUriInfo(), producerResolver, null, getEnitySetName(),
						getEntityKey(), getQueryStringsMap()
								.getFirst("$format"), getQueryStringsMap()
								.getFirst("$callback"), getQueryStringsMap()
								.getFirst("$expand"), getQueryStringsMap()
								.getFirst("$select"));
			}

		} else {
      if (isEntityCountRequest()) {
        String count = "";
        response = new EntitiesRequestResource().getEntitiesCount(httpHeaders,
            getUriInfo(), producerResolver, null, getEnitySetName(),
            count,
            getQueryStringsMap().getFirst("$inlineCount"),
            getQueryStringsMap().getFirst("$top"), getQueryStringsMap()
                .getFirst("$skip"),
            getQueryStringsMap().getFirst("$filter"),
            getQueryStringsMap().getFirst("$orderBy"),
            getQueryStringsMap().getFirst("$format"),
            getQueryStringsMap().getFirst("$callback"),
            getQueryStringsMap().getFirst("$skipToken"),
            getQueryStringsMap().getFirst("$expand"),
            getQueryStringsMap().getFirst("$select"));

      } else {
    	  // let us limit the call to getUriInfo() to this method only, after drop 4, need to apply the logic to the rest
    	  UriInfo uriInfo = new ODataBatchUriInfo(getFullResourceUri(), baseUri);
        response = new EntitiesRequestResource().getEntities(httpHeaders,
            uriInfo, producerResolver, null, getEnitySetName(),
            getQueryStringsMap().getFirst("$inlineCount"),
            getQueryStringsMap().getFirst("$top"), getQueryStringsMap()
                .getFirst("$skip"),
            getQueryStringsMap().getFirst("$filter"),
            getQueryStringsMap().getFirst("$orderBy"),
            getQueryStringsMap().getFirst("$format"),
            getQueryStringsMap().getFirst("$callback"),
            getQueryStringsMap().getFirst("$skipToken"),
            getQueryStringsMap().getFirst("$expand"),
            getQueryStringsMap().getFirst("$select"));
      }
		}

		return response;
	}

	
}
