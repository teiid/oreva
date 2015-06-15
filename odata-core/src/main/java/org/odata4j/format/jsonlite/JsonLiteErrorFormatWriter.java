package org.odata4j.format.jsonlite;

import java.io.Writer;

import javax.ws.rs.core.UriInfo;

import org.odata4j.core.OError;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.json.JsonWriter;
import org.odata4j.producer.ErrorResponse;

/**
 * The Class JsonLiteErrorFormatWriter.
 * 
 * @author <a href="mailto:rajni.kumari@synerzip.com">Rajni Kumari</a>
 */
public class JsonLiteErrorFormatWriter implements FormatWriter<ErrorResponse> {

  private final String jsonpCallback;
  private final String metadataType;

  public JsonLiteErrorFormatWriter(String jsonpCallback, String metadataType) {
    this.jsonpCallback = jsonpCallback;
    this.metadataType = metadataType;
  }

  @Override
  public void write(UriInfo uriInfo, Writer w, ErrorResponse target) {
    JsonWriter jw = new JsonWriter(w);
    jw.startObject();
    writeError(jw, target.getError());
    jw.endObject();
  }

  /**
   * Write error.
   *
   * @param jw the jw
   * @param error the error
   */
  private void writeError(JsonWriter jw, OError error) {
    //{"odata.error":{"code":"","message":{"lang":"en-US","value":"Resource not found for the segment 'Products'."}}

    if (jsonpCallback != null)
      jw.startCallback(jsonpCallback);

    jw.writeName(OdataJsonLiteConstant.ERROR_PROPERTY);
    jw.startObject();
    {
      jw.writeName(OdataJsonLiteConstant.CODE_VALUE);
      jw.writeString(error.getCode());
      jw.writeSeparator();
      jw.writeName(OdataJsonLiteConstant.MESSAGE_VALUE);
      jw.startObject();
      {
        jw.writeName(OdataJsonLiteConstant.LANG_VALUE);
        jw.writeString(OdataJsonLiteConstant.EN_US_VALUE);
        jw.writeSeparator();
        jw.writeName(OdataJsonLiteConstant.VALUE_PROPERTY);
        jw.writeString(error.getMessage());
      }
      jw.endObject();
    }
    jw.endObject();

    if (jsonpCallback != null)
      jw.endCallback();
  }

  @Override
  public String getContentType() {

    if (metadataType == null) {
      return OdataJsonLiteConstant.APPLICATION_JAVASCRIPT_JSONLITE_CHARSET_UTF8;
    }
    else if (metadataType.equalsIgnoreCase(OdataJsonLiteConstant.METADATA_TYPE_FULLMETADATA)) {
      return OdataJsonLiteConstant.APPLICATION_JAVASCRIPT_JSONLITE_FULLMETADATA_CHARSET_UTF8;
    }
    else if (metadataType.equalsIgnoreCase(OdataJsonLiteConstant.METADATA_TYPE_NOMETADATA)) {
      return OdataJsonLiteConstant.APPLICATION_JAVASCRIPT_JSONLITE_NOMETADATA_CHARSET_UTF8;
    }
    return OdataJsonLiteConstant.APPLICATION_JAVASCRIPT_JSONLITE_CHARSET_UTF8;

  }

}
