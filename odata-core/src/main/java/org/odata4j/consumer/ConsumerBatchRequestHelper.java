package org.odata4j.consumer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.odata4j.core.OBatchSupport;
import org.odata4j.core.OChangeSetRequest;
import org.odata4j.core.ODataClientChangeSetResponse;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.ODataVersion;
import org.odata4j.format.Entry;
import org.odata4j.format.FormatType;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.FormatWriterFactory;
import org.odata4j.format.SingleLink;
import org.odata4j.internal.InternalUtil;
import org.odata4j.producer.resources.HeaderMap;

/**
 * 
 * Copyright 2013 Halliburton
 * @author <a href="mailto:peng.chen@halliburton.com">Kevin Chen</a>
 *
 */
public class ConsumerBatchRequestHelper {

  public static String formatSingleRequest(ODataClientRequest req, FormatType formatType) {
    StringBuilder sb = new StringBuilder();

    boolean userDefinedContentType = false;
    sb.append(ODataConstants.Headers.CONTENT_TYPE).append(": ").append(ODataConstants.APPLICATION_HTTP).append("\r\n");
    sb.append(ODataConstants.Headers.CONTENT_TRANSFER_ENCODING).append(": ").append(ODataConstants.BINARY).append("\r\n");
    sb.append("\r\n");

    String url = req.getUrl();
    Map<String, String> queryParams = req.getQueryParams();
    if (queryParams != null && queryParams.size() > 0) {
      UriBuilder ub = UriBuilder.fromUri(req.getUrl());
      for (String qpn : queryParams.keySet()) {
        ub.queryParam(qpn, queryParams.get(qpn));
      }
      url = ub.build().toString();
    }
    // now, adding this request, 1st URL
    sb.append(req.getMethod()).append(" ").append(url).append(" HTTP/1.1\r\n");
    // adding header
    for (String header : req.getHeaders().keySet()) {
      if (header.equals(ODataConstants.Headers.CONTENT_TYPE)) {
        userDefinedContentType = true;
      }
      String value = req.getHeaders().get(header);
      sb.append(header).append(": ").append(value).append("\r\n");
    }

    // request body
    if (req.getPayload() != null) {

      Class<?> payloadClass;
      if (req.getPayload() instanceof Entry)
        payloadClass = Entry.class;
      else if (req.getPayload() instanceof SingleLink)
        payloadClass = SingleLink.class;
      else
        throw new IllegalArgumentException("Unsupported payload: " + req.getPayload());

      StringWriter sw = new StringWriter();
      @SuppressWarnings("unchecked")
      FormatWriter<Object> fw = (FormatWriter<Object>)
          FormatWriterFactory.getFormatWriter(payloadClass, null, formatType.toString(), null);
      fw.write(null, sw, req.getPayload());

      String entity = sw.toString();

      // allow the client to override the default format writer content-type
      if (!userDefinedContentType) {
        sb.append(ODataConstants.Headers.CONTENT_TYPE).append(": ").append(fw.getContentType()).append("\r\n");
      }

      // set content-length 
      sb.append(ODataConstants.Headers.CONTENT_LENGTH).append(": ").append(entity.length() + 2).append("\r\n");

      // now add the pay load for this operation
      sb.append("\r\n\r\n");
      sb.append(entity).append("\r\n");
    }

    return sb.toString();
  }

  public static ODataClientBatchResponse parseSingleOperationResponse(ODataVersion topVersion, String content, OBatchSupport so, FormatType formatType) {
    // first create a buffered reader
    BufferedReader reader = new BufferedReader(new StringReader(content));

    ODataVersion version = topVersion;

    try {
      // 1st line should be status line line HTTP/1.1 200 OK
      String line = reader.readLine();
      String[] statusLine = line.split("\\s");
      int status = Integer.parseInt(statusLine[1]);

      boolean isHeader = true;
      Map<String, String> headers = new HashMap<String, String>();
      MultivaluedMap<String, String> inboundHeaders = new HeaderMap();
      StringBuilder sb = new StringBuilder();
      while ((line = reader.readLine()) != null) {
        // \n\n indicates the end of header for the response
        if (line.isEmpty()) {
          isHeader = false;
          continue;
        }
        if (isHeader) {
          int idx = line.indexOf(":");
          String key = line.substring(0, idx).toUpperCase().trim();
          String value = line.substring(idx + 1).trim();
          headers.put(key, value);
          inboundHeaders.add(key, value);

          if (key.equalsIgnoreCase(ODataConstants.Headers.DATA_SERVICE_VERSION) && value != null && !value.isEmpty()) {
            version = InternalUtil.getDataServiceVersion(value);
          }

        } else {
          sb.append(line);
        }
      }

      Object result = null;
      if (inboundHeaders.containsKey(ODataConstants.Headers.CONTENT_TYPE.toUpperCase())) {
        // we are storing the string inside the batch response, 
        // in this.getFeedReader(), we will check if it is JerseyClientBatchResponse, 
        // if so, it will use parsed String value directly instead of calling workers to
        // parse it agin.
        result = so.getResult(version, sb.toString(), formatType);
      }
      ODataClientBatchResponse ocbr = new ODataClientBatchResponseImpl(status, inboundHeaders, result);

      return ocbr;

    } catch (IOException e) {
      throw new RuntimeException("parseSingleOperationResponse got IOExcepton:", e);
    }

  }

  public static ODataClientBatchResponse parseChangeSetOperationResponse(ODataVersion version, List<String> contentList, OChangeSetRequest csr, FormatType formatType) {

    //the change set will return another list of the result
    ODataClientChangeSetResponse changeSetResult = new ConsumerChangeSetResponseImpl();
    int j = 0;
    for (String content : contentList) {
      OBatchSupport so = csr.getReqs().get(j);
      ODataClientBatchResponse ocbr = ConsumerBatchRequestHelper.parseSingleOperationResponse(version, content, so, formatType);
      changeSetResult.add(ocbr);
      j++;
    }

    return changeSetResult;
  }

}
