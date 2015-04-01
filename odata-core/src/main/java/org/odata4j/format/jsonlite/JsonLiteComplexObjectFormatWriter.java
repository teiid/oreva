package org.odata4j.format.jsonlite;

import javax.ws.rs.core.UriInfo;

import org.odata4j.format.json.JsonWriter;
import org.odata4j.producer.ComplexObjectResponse;

/**
 * Writer for OComplexObjects in JSON-LITE
 * 
 * @author <a href="mailto:rajni.kumari@synerzip.com">Rajni Kumari</a>
 * 
 */
public class JsonLiteComplexObjectFormatWriter extends JsonLiteFormatWriter<ComplexObjectResponse> {

  private final String metadataType;
  
  /**
   * Instantiates a new json lite complex object format writer.
   *
   * @param jsonpCallback the jsonp callback
   * @param metadataType 
   */
  public JsonLiteComplexObjectFormatWriter(String jsonpCallback, String metadataType) {
    super(jsonpCallback, metadataType);
    this.metadataType = metadataType;
  }

  @Override
  protected void writeContent(UriInfo uriInfo, JsonWriter jw, ComplexObjectResponse target) {
    // {
    //  "odata.metadata": "http://.../Soupon.svc/$metadata#Soupon.BillingAddress",
    // "Line1": "12345 Grant Street",
    //  "Line2": null,
    //  "City": "Taft",
    //   "State": "Ohio",
    //   "ZipCode": "98052"
    //  }
    if (metadataType != null && !metadataType.equalsIgnoreCase(OdataJsonLiteConstant.METADATA_TYPE_NOMETADATA)) {
      jw.writeName(OdataJsonLiteConstant.METADATA_PROPERTY);
      jw.writeString(uriInfo.getBaseUri() + OdataJsonLiteConstant.METADATA_PROPERTY_WITH_HASH_DOLLAR + target.getObject().getType().getFullyQualifiedTypeName());
      jw.writeSeparator();
    }
    super.writeOProperties(jw, target.getObject().getProperties(), true);
  }

}
