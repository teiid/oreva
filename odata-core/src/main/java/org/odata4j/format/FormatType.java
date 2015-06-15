package org.odata4j.format;

import javax.ws.rs.core.MediaType;

import org.odata4j.core.ODataConstants;
import org.odata4j.format.jsonlite.OdataJsonLiteConstant;

public enum FormatType {

  ATOM(MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML),
  JSONVERBOSE(MediaType.valueOf(ODataConstants.APPLICATION_JAVASCRIPT_VERBOSE).toString()),
  JSON(MediaType.APPLICATION_JSON, MediaType.valueOf(OdataJsonLiteConstant.FORMAT_TYPE_JSONLITE).toString()),
  JSONLITEFULLMETADATA(MediaType.valueOf(OdataJsonLiteConstant.FORMAT_TYPE_JSONLITE_FULLMETADATA).toString()),
  JSONLITENOMETADATA(MediaType.valueOf(OdataJsonLiteConstant.FORMAT_TYPE_JSONLITE_NOMETADATA).toString());

  private FormatType(String... mediaTypes) {
    this.mediaTypes = mediaTypes;
  }

  private final String[] mediaTypes;

  public String[] getAcceptableMediaTypes() {
    return mediaTypes;
  }

  public static FormatType parse(String format) {
    if ("verbosejson".equalsIgnoreCase(format) || "jsonverbose".equalsIgnoreCase(format))
      return JSONVERBOSE;
    else if ("atom".equalsIgnoreCase(format))
      return ATOM;
    else if ("json".equalsIgnoreCase(format) || "json;odata=minimalmetadata".equalsIgnoreCase(format))
      return JSON;
    else if ("json;odata=nometadata".equalsIgnoreCase(format) || "jsonlitenometadata".equalsIgnoreCase(format))
      return JSONLITENOMETADATA;
    else if ("json;odata=fullmetadata".equalsIgnoreCase(format) || "jsonlitefullmetadata".equalsIgnoreCase(format))
      return JSONLITEFULLMETADATA;
    throw new UnsupportedOperationException("Unsupported format " + format);
  }
}
