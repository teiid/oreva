package org.odata4j.format.jsonlite;

import javax.ws.rs.core.UriInfo;

import org.odata4j.format.json.JsonWriter;
import org.odata4j.producer.PropertyResponse;

/**
 * The Class JsonLitePropertyFormatWriter.
 * 
 * @author <a href="mailto:rajni.kumari@synerzip.com">Rajni Kumari</a>
 */
public class JsonLitePropertyFormatWriter extends JsonLiteFormatWriter<PropertyResponse> {

  private final String metadataType;
  
  public JsonLitePropertyFormatWriter(String jsonpCallback, String metadataType) {
    super(jsonpCallback, metadataType);
    this.metadataType = metadataType;
  }

  @Override
  protected void writeContent(UriInfo uriInfo, JsonWriter jw, PropertyResponse target) {
    if (metadataType != null && !metadataType.equalsIgnoreCase(OdataJsonLiteConstant.METADATA_TYPE_NOMETADATA)) {
      jw.writeName(OdataJsonLiteConstant.METADATA_PROPERTY);
      jw.writeString(uriInfo.getBaseUri().toString() + OdataJsonLiteConstant.METADATA_PROPERTY_WITH_HASH_DOLLAR + target.getProperty().getType().getFullyQualifiedTypeName());
      jw.writeSeparator();
    }
    jw.writeName(OdataJsonLiteConstant.VALUE_PROPERTY);
    writeValue(jw, target.getProperty().getType(), target.getProperty().getValue(), true);

  }

}
