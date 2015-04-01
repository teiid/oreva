package org.odata4j.examples.jersey.consumer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.StatusType;

import org.core4j.Enumerable;
import org.core4j.xml.XDocument;
import org.core4j.xml.XmlFormat;
import org.odata4j.consumer.AbstractODataClient;
import org.odata4j.consumer.ConsumerBatchRequestHelper;
import org.odata4j.consumer.ODataClientBatchResponse;
import org.odata4j.consumer.ODataClientRequest;
import org.odata4j.consumer.ODataClientResponse;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.behaviors.OClientBehavior;
import org.odata4j.consumer.behaviors.OClientBehaviors;
import org.odata4j.core.OBatchSupport;
import org.odata4j.core.OChangeSetRequest;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.ODataConstants.Charsets;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OError;
import org.odata4j.core.OErrors;
import org.odata4j.core.Throwables;
import org.odata4j.exceptions.BadRequestException;
import org.odata4j.exceptions.ODataProducerException;
import org.odata4j.exceptions.ODataProducerExceptions;
import org.odata4j.exceptions.ServerErrorException;
import org.odata4j.format.Entry;
import org.odata4j.format.FormatParserFactory;
import org.odata4j.format.FormatType;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.FormatWriterFactory;
import org.odata4j.format.Parameters;
import org.odata4j.format.SingleLink;
import org.odata4j.internal.BOMWorkaroundReader;
import org.odata4j.internal.InternalUtil;
import org.odata4j.stax2.XMLEventReader2;
import org.odata4j.stax2.util.StaxUtil;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.PartialRequestBuilder;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.reader.HttpHeaderReader;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.MultiPart;

/**
 * OData client based on Jersey.
 */
class ODataJerseyClient extends AbstractODataClient {
  
  public static final String MULTIPART_BASE = "multipart";
  public static final MediaType MULTIPART_BASE_TYPE = new MediaType(MULTIPART_BASE, null);


  private final OClientBehavior[] requiredBehaviors = new OClientBehavior[] { OClientBehaviors.methodTunneling("MERGE") }; // jersey hates MERGE, tunnel through POST
  private final OClientBehavior[] behaviors;

  private final Client client;

  public ODataJerseyClient(FormatType type, JerseyClientFactory clientFactory, OClientBehavior... behaviors) {
    super(type);
    this.behaviors = Enumerable.create(requiredBehaviors).concat(Enumerable.create(behaviors)).toArray(OClientBehavior.class);
    this.client = JerseyClientUtil.newClient(clientFactory, behaviors);
  }
  
  /**
   * Sets the ChunkedEncodingSize for jersey client.
   */
  private int setClientChunKSize() {
	  //default to 32 MB
    int clientChunKSize = 32 * 1024 * 1024;;
    String chunkedEncodingSizeVarValue = InternalUtil.getSystemPropertyValue(ODataConstants.JERSEY_CLIENT_CHUNKED_ENCODING_SIZE);
    if (chunkedEncodingSizeVarValue != null && !chunkedEncodingSizeVarValue.isEmpty()) {
      try {
    	//The value passed on the system variable is in MB and we need to convert it to bytes
    	  clientChunKSize= Integer.parseInt(chunkedEncodingSizeVarValue) * 1024 * 1024;;
      } catch (NumberFormatException numFormatException) {
        // We ignore the exception and use default;
      }
    }
    return clientChunKSize;
  }

  public Reader getFeedReader(ODataClientResponse response) {
    ClientResponse clientResponse = ((JerseyClientResponse) response).getClientResponse();
    if (ODataConsumer.dump.responseBody()) {
      String textEntity = clientResponse.getEntity(String.class);
      dumpResponseBody(textEntity, clientResponse.getType());
      return new BOMWorkaroundReader(new StringReader(textEntity));
    }

    InputStream textEntity = clientResponse.getEntityInputStream();
    try {
      return new BOMWorkaroundReader(new InputStreamReader(textEntity, Charsets.Upper.UTF_8));
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  public String requestBody(FormatType formatType, ODataClientRequest request) throws ODataProducerException {
    ODataClientResponse response = doRequest(formatType, request, Status.OK);
    String entity = ((JerseyClientResponse) response).getClientResponse().getEntity(String.class);
    response.close();
    return entity;
  }

  /**
   * This is consumer side to create a batch request, and then handle the response.
   * @param batchRequest
   * the batchRequest is a POST with end point $batch, the content-type should be multipart/mixed.
   * @param childRequests
   * this is a list of the operation that will be part of the batch request. it can also contain
   * change set.
   * 
   */
  @Override
  public List<ODataClientBatchResponse> batchRequest(FormatType reqType1, ODataClientRequest batchRequest, List<?> childRequests) {

    List<ODataClientBatchResponse> result = null;
    if (behaviors != null) {
      for (OClientBehavior behavior : behaviors)
        batchRequest = behavior.transform(batchRequest);
    }

    WebResource webResource = JerseyClientUtil.resource(client, batchRequest.getUrl(), behaviors);
    WebResource.Builder b = webResource.getRequestBuilder();
    String boundary = null;
    String cType = null;

    FormatType formatType = getFormatType();
    // set headers
    b = b.accept(formatType.getAcceptableMediaTypes());

    for (String header : batchRequest.getHeaders().keySet()) {
      // parse content type to get the boundary string
      if (header.equals(ODataConstants.Headers.CONTENT_TYPE)) {
        cType = batchRequest.getHeaders().get(header);
        boundary = cType.substring(cType.indexOf('=') + 1);
      }
      b.header(header, batchRequest.getHeaders().get(header));
    }

    if (ODataConsumer.dump.requestHeaders())
      dumpHeaders(batchRequest, webResource, b);

    if (boundary == null) {
      throw new BadRequestException("batchRequest's content type should contain boundary");
    }
    // now create the pay load for the batch request
    StringBuilder sb = new StringBuilder();
    for (Object req : childRequests) {
      // new way to add the request
      sb.append("\n--").append(boundary).append("\n");
      sb.append(((OBatchSupport) req).formatRequest(this.getFormatType()));
    }

    // ending the batch multi part
    if (childRequests.size() > 0) {
      sb.append("\n--").append(boundary).append("--\n");
    }

    String entity = sb.toString();
    b.entity(entity, cType);

    if (ODataConsumer.dump.requestBody()) {
      dump(entity);
    }

    // execute request
    ClientResponse response = null;
    try {
      response = b.method(batchRequest.getMethod(), ClientResponse.class);
      Integer status = response.getStatus();
      String responseContentType = response.getHeaders().getFirst(ODataConstants.Headers.CONTENT_TYPE);
      MediaType mType = getMediaType(responseContentType);
      // check the response if it is multi part, if not, an error occured, throw exception
      if (!mType.isCompatible(MULTIPART_BASE_TYPE)) {
        String errMsg = response.getEntity(String.class);
        OError error = OErrors.error(status.toString(), errMsg, null);
        throw new ServerErrorException.Factory().createException(error);
      }

      result = parseResponse(response, childRequests);

    } catch (ClientHandlerException e) {
      Throwables.propagate(e);
    }

    return result;
  }

  private List<ODataClientBatchResponse> parseResponse(ClientResponse response, List<?> childRequests) {

    ODataVersion version = InternalUtil.getDataServiceVersion(response.getHeaders()
        .getFirst(ODataConstants.Headers.DATA_SERVICE_VERSION));

    MultiPart mp = response.getEntity(MultiPart.class); // input stream can only be consumed once

    // this is the list will hold individual request result.
    List<ODataClientBatchResponse> batchResultList = new ArrayList<ODataClientBatchResponse>(childRequests.size());

    if (ODataConsumer.dump.responseHeaders()) {
      dumpHeaders(response);
    }

    int i = 0;
    for (BodyPart bp : mp.getBodyParts()) {
      ODataClientBatchResponse ocbr = null;
      MediaType cType = bp.getMediaType();
      if (cType.isCompatible(new MediaType("multipart", "mixed"))) {
        MultiPart cmp = bp.getEntityAs(MultiPart.class);
        OChangeSetRequest csr = (OChangeSetRequest) childRequests.get(i);

        List<String> payloadList = new ArrayList<String>();

        for (BodyPart cbp : cmp.getBodyParts()) {
          String content = cbp.getEntityAs(String.class);
          payloadList.add(content);
        }
        ocbr = ConsumerBatchRequestHelper.parseChangeSetOperationResponse(version, payloadList, csr, getFormatType());
      } else {
        String content = bp.getEntityAs(String.class);
        OBatchSupport so = (OBatchSupport) childRequests.get(i);
        ocbr = ConsumerBatchRequestHelper.parseSingleOperationResponse(version, content, so, getFormatType());
      }

      batchResultList.add(ocbr);
      i++;
    }

    return batchResultList;
  }

  @SuppressWarnings("unchecked")
  protected ODataClientResponse doRequest(FormatType reqType, ODataClientRequest request, StatusType... expectedResponseStatus) throws ODataProducerException {

    if (behaviors != null) {
      for (OClientBehavior behavior : behaviors)
        request = behavior.transform(request);
    }
    if(request.getPayload() != null && request.getPayload() instanceof InputStream) {
    	 this.client.setChunkedEncodingSize(setClientChunKSize());
    }

    WebResource webResource = JerseyClientUtil.resource(client, request.getUrl(), behaviors);

    // set query params
    for (String qpn : request.getQueryParams().keySet())
      webResource = webResource.queryParam(qpn, request.getQueryParams().get(qpn));

    WebResource.Builder b = webResource.getRequestBuilder();

    // set headers
    b = b.accept(reqType.getAcceptableMediaTypes());

    for (String header : request.getHeaders().keySet())
      b.header(header, request.getHeaders().get(header));
    if (!request.getHeaders().containsKey(ODataConstants.Headers.USER_AGENT))
      b.header(ODataConstants.Headers.USER_AGENT, "odata4j.org");

    if (ODataConsumer.dump.requestHeaders())
      dumpHeaders(request, webResource, b);

    // request body
    if (request.getPayload() != null) {

      Class<?> payloadClass;
      if (request.getPayload() instanceof Entry)
        payloadClass = Entry.class;
      else if (request.getPayload() instanceof SingleLink)
        payloadClass = SingleLink.class;
      else if (request.getPayload() instanceof Parameters)
        payloadClass = Parameters.class;
      else if (request.getPayload() instanceof InputStream)
        payloadClass = InputStream.class;
      else
        throw new IllegalArgumentException("Unsupported payload: " + request.getPayload());

      if (request.getPayload() instanceof InputStream) {
        // send media stream as payload
        String contentType = request.getHeaders().containsKey(ODataConstants.Headers.CONTENT_TYPE)
            ? request.getHeaders().get(ODataConstants.Headers.CONTENT_TYPE)
            : ODataConstants.APPLICATION_OCTET_STREAM;
        b.entity(request.getPayload(), contentType);
      } else {
        StringWriter sw = new StringWriter();
        FormatWriter<Object> fw = (FormatWriter<Object>)
            FormatWriterFactory.getFormatWriter(payloadClass, null, this.getFormatType().toString(), null);
        fw.write(null, sw, request.getPayload());

        String entity = sw.toString();
        if (ODataConsumer.dump.requestBody())
          dump(entity);

        // allow the client to override the default format writer content-type
        String contentType = request.getHeaders().containsKey(ODataConstants.Headers.CONTENT_TYPE)
            ? request.getHeaders().get(ODataConstants.Headers.CONTENT_TYPE)
            : fw.getContentType();

        b.entity(entity, contentType);
      }
    }

    // execute request
    ClientResponse response = null;
    try {
      response = b.method(request.getMethod(), ClientResponse.class);
    } catch (ClientHandlerException e) {
      Throwables.propagate(e);
    }

    if (ODataConsumer.dump.responseHeaders())
      dumpHeaders(response);
    StatusType status = response.getClientResponseStatus();
    for (StatusType expStatus : expectedResponseStatus)
      if (expStatus.getStatusCode() == status.getStatusCode())
        return new JerseyClientResponse(response);

    // the server responded with an unexpected status
    RuntimeException exception;
    String textEntity = response.getEntity(String.class); // input stream can only be consumed once
    try {
      // report error as ODataProducerException in case we get a well-formed OData error...
      MediaType contentType = response.getType();
      OError error = FormatParserFactory.getParser(OError.class, contentType, null).parse(new StringReader(textEntity));
      exception = ODataProducerExceptions.create(status, error);
    } catch (RuntimeException e) {
      // ... otherwise throw a RuntimeError
      exception = new RuntimeException(String.format("Expected status %s, found %s. Server response:",
          Enumerable.create(expectedResponseStatus).join(" or "), status) + "\n" + textEntity, e);
    }
    throw exception;
  }

  protected XMLEventReader2 toXml(ODataClientResponse response) {
    ClientResponse clientResponse = ((JerseyClientResponse) response).getClientResponse();

    if (ODataConsumer.dump.responseBody()) {
      String textEntity = clientResponse.getEntity(String.class);
      dumpResponseBody(textEntity, clientResponse.getType());
      return StaxUtil.newXMLEventReader(new BOMWorkaroundReader(new StringReader(textEntity)));
    }

    InputStream textEntity = clientResponse.getEntityInputStream();
    try {
      return StaxUtil.newXMLEventReader(new BOMWorkaroundReader(new InputStreamReader(textEntity, Charsets.Upper.UTF_8)));
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  private void dumpResponseBody(String textEntity, MediaType type) {
    String logXml = textEntity;
    if (type.toString().contains("xml") || logXml != null && logXml.startsWith("<feed")) {
      try {
        logXml = XDocument.parse(logXml).toString(XmlFormat.INDENTED);
      } catch (Exception ignore) {}
    }
    dump(logXml);
  }

  private void dumpHeaders(ClientResponse response) {
    dump("Status: " + response.getStatus());
    dump(response.getHeaders());
  }

  private static boolean dontTryRequestHeaders;

  @SuppressWarnings("unchecked")
  private MultivaluedMap<String, Object> getRequestHeaders(WebResource.Builder b) {
    if (dontTryRequestHeaders)
      return null;

    //  protected MultivaluedMap<String, Object> metadata;
    try {
      Field f = PartialRequestBuilder.class.getDeclaredField("metadata");
      f.setAccessible(true);
      return (MultivaluedMap<String, Object>) f.get(b);
    } catch (Exception e) {
      dontTryRequestHeaders = true;
      return null;
    }

  }

  private void dumpHeaders(ODataClientRequest request, WebResource webResource, WebResource.Builder b) {
    dump(request.getMethod() + " " + webResource);
    dump(getRequestHeaders(b));
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void dump(MultivaluedMap headers) {
    if (headers == null)
      return;

    for (Object header : headers.keySet())
      dump(header + ": " + headers.getFirst(header));
  }

  private static void dump(String message) {
    System.out.println(message);
  }

  /**
   * create the MediaType instance based on content type
   * @param contentType
   * @return
   */
  public static MediaType getMediaType(String contentType) {
    try {
      List<MediaType> list = new ArrayList<MediaType>();
      list = HttpHeaderReader.readMediaTypes(list, contentType);
      return list.get(0);
    } catch (ParseException e) {
      throw new IllegalArgumentException("cannot parse the content type " + contentType);
    }
  }

  @Override
  public Reader getFeedReader(String textEntity) {
    if (ODataConsumer.dump.responseBody()) {
      dump(textEntity);
    }
    return new BOMWorkaroundReader(new StringReader(textEntity));
  }
}
