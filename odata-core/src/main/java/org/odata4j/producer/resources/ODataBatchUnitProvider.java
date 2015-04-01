package org.odata4j.producer.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.odata4j.producer.resources.ODataBatchProvider.HTTP_METHOD;

/**
 * 
 * Copyright 2013 Halliburton
 * @author <a href="mailto:peng.chen@halliburton.com">Kevin Chen</a>
 *
 */
@Provider
@Consumes(ODataBatchProvider.MULTIPART_MIXED)
public class ODataBatchUnitProvider implements
		MessageBodyReader<List<ODataBatchUnit>> {

	@Context
	HttpHeaders httpHeaders;
	@Context
	UriInfo uriInfo;

	final String ContentType = "content-type:";

	@Override
	public boolean isReadable(Class<?> type, Type genericType,
			Annotation[] antns, MediaType mt) {
		if (genericType instanceof ParameterizedType) {
			for (Type gType : ((ParameterizedType) genericType)
					.getActualTypeArguments()) {
				if (gType == ODataBatchUnit.class && type == List.class) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public List<ODataBatchUnit> readFrom(Class<List<ODataBatchUnit>> type,
			Type genericType, Annotation[] antns, MediaType mt,
			MultivaluedMap<String, String> mm, InputStream inputStream)
			throws IOException, WebApplicationException {

		BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));
    return parse(mt, br);

  }

  private List<ODataBatchUnit> parse(MediaType mt, BufferedReader br) throws IOException {
    String currentLine = "";
    StringBuilder sb = null;
    List<ODataBatchUnit> parts = new ArrayList<ODataBatchUnit>();

    String boundary = mt.getParameters().get("boundary");
    boundary = "--" + boundary;
    String endBatchBoundary = boundary + "--";
    while ((currentLine = br.readLine()) != null) {
      // check if it is start of a new boundary
      if (currentLine.equals(boundary)) {
        if (sb != null) {
          ODataBatchUnit part = parseOnePart(sb.toString());
          if (parts != null)
            parts.add(part);
        }
        sb = new StringBuilder();
      } else if (currentLine.equals(endBatchBoundary)) {
        if (sb != null) {
          ODataBatchUnit part = parseOnePart(sb.toString());
          if (parts != null)
            parts.add(part);
        }
        return parts;
      } else {
        if (sb != null) {
          sb.append(currentLine).append("\n");
        }
      }
    }

    if (sb != null) {
      ODataBatchUnit part = parseOnePart(sb.toString());
      if (part != null) {
        parts.add(part);
      }
    }
    return parts;
  }


  private ODataBatchUnit parseOnePart(String content) throws IOException {
    boolean isHeader = true;
    boolean isChangeSet = false;
    String cType = null;

    BufferedReader br = new BufferedReader(new StringReader(content));
    String currentLine = null;
    //    StringBuilder sb = new StringBuilder();
    ODataBatchUnit unit = null;

    while ((currentLine = br.readLine()) != null) {
      if (isHeader) {
        // check if this is the end of the header
        if (currentLine.trim().equals("")) {
          isHeader = false;
          if (isChangeSet) {
            unit = parseChangeSet(cType, br);
            br.close();
            return unit;
          } else {
            unit = parseSingleUnit(br);
            br.close();
            return unit;
          }
        } else {
          if (cType == null && currentLine.toLowerCase().startsWith(ContentType)) {
            cType = currentLine.substring(ContentType.length()).trim();
            if (cType.startsWith(ODataBatchProvider.MULTIPART_MIXED)) {
              isChangeSet = true;
            } else if (cType.toLowerCase().startsWith("application/http")) {
              isChangeSet = false;
            }
          }
        }
      }
    }

    br.close();
    return null;
  }

  private ODataBatchUnit parseSingleUnit(BufferedReader br) throws IOException {
    String currentLine = null;
    HTTP_METHOD httpMethod = null;
    String uri = null;
    boolean isHeader = true;
    MultivaluedMap<String, String> headers = new HeaderMap();
    StringBuilder sb = new StringBuilder();

    while ((currentLine = br.readLine()) != null) {
      if (httpMethod == null) {
        for (HTTP_METHOD method : HTTP_METHOD.values()) {
          currentLine = currentLine.trim();
          if (currentLine.startsWith(method.name())) {
            uri = currentLine
                .substring(method.name().length() + 1);
            int lastIdx = uri.lastIndexOf(" ");
            if (lastIdx != -1) {
              uri = uri.substring(0, lastIdx).trim();
            }
            httpMethod = method;
            break;
          }
        }
      } else {
        if (isHeader) {
          if (currentLine.isEmpty()) {
            isHeader = false;
          } else {
            addHeader(currentLine, headers);
          }
        } else {
          sb.append(currentLine);
        }

      }
    }

    // the 2 null will be provided by context injection
    return create(httpMethod, httpHeaders, uriInfo, uri, sb.toString(), headers);
  }

  private ODataBatchUnit parseChangeSet(String cType, BufferedReader br) throws IOException {
    MediaType mt = BatchRequestResource.getMediaType(cType);
    List<ODataBatchUnit> childList = parse(mt, br);

    ODataBatchChangeSetUnit changeSet = new ODataBatchChangeSetUnit();
    for (ODataBatchUnit unit : childList) {
      changeSet.addPart((ODataBatchSingleUnit) unit);
    }

    System.out.println("change set parts: " + changeSet.getParts().size());
    return changeSet;
  }

  private void addHeader(String currentLine, MultivaluedMap<String, String> headers) {
    Integer idx = currentLine.indexOf(':');
    String key = currentLine.substring(0, idx);
    String value = currentLine.substring(idx + 1).trim();
    headers.putSingle(key, value);

  }

  private ODataBatchUnit create(HTTP_METHOD httpMethod, HttpHeaders topHttpHeaders, UriInfo topUriInfo,
      String uri, String contents, MultivaluedMap<String, String> headers) {
    ODataBatchUnit unit = null;

    try {
      switch (httpMethod) {
      case POST:
        unit = new ODataBatchPostUnit(topHttpHeaders, topUriInfo,
            uri, contents, headers);
        break;

      case MERGE:
        unit = new ODataBatchMergeUnit(topHttpHeaders, topUriInfo,
            uri, contents, headers);
        break;

      case DELETE:
        unit = new ODataBatchDelUnit(topHttpHeaders, topUriInfo,
            uri, contents, headers);
        break;

      case GET:
        unit = new ODataBatchGetUnit(topHttpHeaders, topUriInfo,
            uri, headers);
        break;

      case PUT:
        unit = new ODataBatchPutUnit(topHttpHeaders, topUriInfo,
            uri, contents, headers);
        break;
      }
    } catch (URISyntaxException e) {
      unit = null;
    }
    return unit;
  }



}
