package org.odata4j.producer.resources;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;

import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityIds;
import org.odata4j.core.OEntityKey;
import org.odata4j.format.FormatParser;
import org.odata4j.format.FormatParserFactory;
import org.odata4j.format.SingleLink;
import org.odata4j.producer.ODataProducer;
import org.odata4j.urlencoder.ConversionUtil;

/**
 * 
 * Copyright 2013 Halliburton
 * @author <a href="mailto:peng.chen@halliburton.com">Kevin Chen</a>
 *
 */
public abstract class ODataBatchSingleUnit extends ODataBatchUnit {
  final private UriInfo uriInformation;
  // this is the uri for individual batch operation, 
  final private URI fullResourceUri;
  // after parsing the full uri, the query string will be put into the map
  private MultivaluedMap<String, String> queryMap = null;
  // the entity name corresponding to the table name
  private String entitySetName;
  // if uri contain the primary key to locate a entity
  private String entityKey;
  // navigation property
  private String navProperty;
  final private String resourceContents;
  final private MultivaluedMap<String, String> resourceHeaders;
  private boolean isParsed = false;
  private boolean hasLinkProperty = false;
  private String linkTargetProperty;
  private String linkTargetId;
  private Response intermediateResponse;

  /**
   * Gets the intermediate response.
   *
   * @return the intermediate response
   */
  public Response getIntermediateResponse() {
    return intermediateResponse;
  }

  /**
   * Sets the intermediate response.
   *
   * @param intermediateResponse the new intermediate response
   */
  public void setIntermediateResponse(Response intermediateResponse) {
    this.intermediateResponse = intermediateResponse;
  }

  protected abstract Response delegate(HttpHeaders httpHeaders, URI baseUri, ContextResolver<ODataProducer> producerResolver) throws Exception;

  protected ODataBatchSingleUnit(UriInfo uriInfo, String uri, String contents, MultivaluedMap<String, String> headers) throws URISyntaxException {
    uriInformation = uriInfo;
    fullResourceUri = new URI(uri);
    resourceContents = contents;
    resourceHeaders = headers;
  }

  protected String createResponse(Response response) {
    StringBuilder batchResponse = new StringBuilder();
    batchResponse.append("\n").append(ODataConstants.Headers.CONTENT_TYPE).append(": application/http");
    batchResponse.append("\nContent-Transfer-Encoding: binary\n");
    batchResponse.append(BatchRequestResource.createResponseBodyPart(this, response));

    return batchResponse.toString();
  }

  @Override
  public final Response execute(HttpHeaders httpHeaders, ContextResolver<ODataProducer> producerResolver, URI baseUri) throws Exception {

    if (!fullResourceUri.toString().startsWith(baseUri.toString())) {
      throw new UnsupportedOperationException("the resouce url does not match base url from batch operation,\n\tbaseUri=" + baseUri +
          "\n\trequest url=" + fullResourceUri);
    }

    if (!isParsed) {
      parseUri(fullResourceUri, baseUri);
      isParsed = true;
    }
    return delegate(httpHeaders, baseUri, producerResolver);
  }

  /**
   * Creates the response for batch.
   *
   * @param httpHeaders the http headers
   * @param producerResolver the producer resolver
   * @param baseUri the base uri
   * @param requestedEntity the requested entity
   * @return the response
   * @throws Exception the exception
   */
  public Response createResponseForBatch(HttpHeaders httpHeaders, ContextResolver<ODataProducer> producerResolver, URI baseUri, String requestedEntity) throws Exception {
    ODataProducer producer = producerResolver.getContext(ODataProducer.class);
    OEntityKey entityKey = null;
    //setting isResponse to true, since the entity we get from the response contain not only name and value pairs, but also metadata, relations,etc
    //so that Json parser will parse it and give you back OEntity.
    Boolean isResponse = true;
    BatchRequestResource batchRequestResource = new BatchRequestResource();
    OEntity entity = batchRequestResource.getRequestEntity(httpHeaders, getResourceHeaders(), getUriInfo(), requestedEntity, producer.getMetadata(), entitySetName, entityKey, isResponse);

    return batchRequestResource.createResponseForBatch(httpHeaders, getUriInfo(), producer, getEnitySetName(), entity, getMediaTypeListForBatch());
  }

  public UriInfo getUriInfo() {
    return uriInformation;
  }

  public URI getFullResourceUri() {
    return fullResourceUri;
  }

  public MultivaluedMap<String, String> getQueryStringsMap() {
    return queryMap;
  }

  public String getEnitySetName() {
    return entitySetName;
  }

  public String getEntityKey() {
    return entityKey;
  }

  public String getResourceContent() {
    return resourceContents;
  }

  public MultivaluedMap<String, String> getResourceHeaders() {
    return resourceHeaders;
  }

  public MediaType getResourceMediaType() {
    String contentType = resourceHeaders.getFirst(ODataConstants.Headers.CONTENT_TYPE);

    MediaType type = BatchRequestResource.getMediaType(contentType);

    return type;

  }

  /**
   * Gets the media type list for batch.
   *
   * @return the media type list for batch
   */
  public List<MediaType> getMediaTypeListForBatch() {
    List<MediaType> mediaTypeList = null;
    MediaType mediaType = getResourceMediaType();
    if (mediaType != null) {
      mediaTypeList = new ArrayList<MediaType>();
      mediaTypeList.add(mediaType);
    }
    return mediaTypeList;
  }

  /**
   * @return the navProperty
   */
  public String getNavProperty() {
    return navProperty;
  }

  public boolean isEntityCountRequest() {
    if (navProperty != null && navProperty.toLowerCase().equals("$count")) {
      return true;
    }

    return false;
  }

  public boolean hasLinkProperty() {
    return hasLinkProperty;
  }

  public String getLinkTargetProperty() {
    return linkTargetProperty;
  }

  public String getLinkTargetId() {
    return linkTargetId;
  }

  protected LinksRequestResource getLinkRequestResouce() {
    if (!hasLinkProperty) {
      throw new UnsupportedOperationException("the request is not a link request: " + fullResourceUri);
    }

    OEntityKey targetEntityKey = linkTargetId == null || linkTargetId.isEmpty() ? null : OEntityKey.parse(linkTargetId);
    return new LinksRequestResource(OEntityIds.create(entitySetName, OEntityKey.parse(entityKey)), linkTargetProperty, targetEntityKey);
  }

  @Override
  public String getBatchUnitContentType() {
    return "\r\nContent-Type: application/http\r\nContent-Transfer-Encoding: binary";
  }

  /**
   * parse the request uri to create query String Map
   * @param baseUri 
   * @return
   * @throws URISyntaxException 
   */
  private void parseUri(URI fullUri, URI baseUri) {

    // $link in odata spec
    String linkPatten = "$links/";

    // get the query string from the request
    queryMap = ConversionUtil.decodeQueryString(fullResourceUri);

    // get the entity/navigation part without query string
    URI relUri = baseUri.relativize(fullUri);
    String entityUriString = relUri.getPath();
    // remove starting / if existed
    if (entityUriString.startsWith("/")) {
      entityUriString = entityUriString.substring(1);
    }

    int i = entityUriString.indexOf('/');
    if (i != -1) {
      entitySetName = entityUriString.substring(0, i).trim();
      navProperty = entityUriString.substring(i + 1).trim();
    } else {
      entitySetName = entityUriString;
    }

    // now check if the entity set has primary key associated with it
    i = entitySetName.indexOf('(');
    if (i != -1) {
      entityKey = entitySetName.substring(i + 1, entitySetName.length() - 1).trim();
      entitySetName = entitySetName.substring(0, i).trim();
    }

    // now check if the navigation property is a link property
    if (navProperty != null && navProperty.startsWith(linkPatten)) {
      hasLinkProperty = true;
      linkTargetProperty = navProperty.substring(linkPatten.length());
      i = linkTargetProperty.indexOf('(');
      if (i != -1) {
        int j = linkTargetProperty.lastIndexOf(')');
        if (j != -1) {
          linkTargetId = linkTargetProperty.substring(i + 1, j).trim();
        } else {
          linkTargetId = linkTargetProperty.substring(i + 1).trim();
        }

        linkTargetProperty = linkTargetProperty.substring(0, i).trim();
      }
    }

    return;
  }

  /**
   * Helper method to parse link uri to get the link target's entity id.
   * @param httpHeaders the batch request header
   * @param uriInfo batch request uri info
   * @param payload link request payload
   * @return entity id
   */
  protected OEntityId parseLinkRequestUri(HttpHeaders httpHeaders, UriInfo uriInfo, String payload) {
    FormatParser<SingleLink> parser = FormatParserFactory.getParser(SingleLink.class, getResourceMediaType(), null);
    SingleLink link = parser.parse(new StringReader(payload));
    return OEntityIds.parse(uriInfo.getBaseUri().toString(), link.getUri());
  }

}
