package org.odata4j.format.jsonlite;

import javax.ws.rs.core.UriInfo;

import org.odata4j.format.json.JsonWriter;
import org.odata4j.producer.SimpleResponse;

/**
 * write a single value that has an EdmSimpleType type
 * 
 * @author <a href="mailto:rajni.kumari@synerzip.com">Rajni Kumari</a>
 */
public class JsonLiteSimpleFormatWriter extends JsonLiteFormatWriter<SimpleResponse> {
  private final String metadataType;

  public JsonLiteSimpleFormatWriter(String jsonpCallback, String metadataType) {
    super(jsonpCallback, metadataType);
    this.metadataType = metadataType;
  }

  @Override
  protected void writeContent(UriInfo uriInfo, JsonWriter jw, SimpleResponse target) {
    if (metadataType != null && !metadataType.equalsIgnoreCase(OdataJsonLiteConstant.METADATA_TYPE_NOMETADATA)) {
      jw.writeName(OdataJsonLiteConstant.METADATA_PROPERTY);
      jw.writeString(uriInfo.getBaseUri().toString() + OdataJsonLiteConstant.METADATA_PROPERTY_WITH_HASH_DOLLAR + target.getType().getFullyQualifiedTypeName());
      jw.writeSeparator();
    }
    jw.writeName(OdataJsonLiteConstant.VALUE_PROPERTY);
    writeValue(jw, target.getType(), target.getValue(), true);

  }
}
