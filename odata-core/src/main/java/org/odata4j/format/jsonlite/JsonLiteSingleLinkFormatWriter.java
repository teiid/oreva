package org.odata4j.format.jsonlite;

import javax.ws.rs.core.UriInfo;

import org.odata4j.format.SingleLink;
import org.odata4j.format.json.JsonWriter;

/**
 * The Class JsonLiteSingleLinkFormatWriter.
 * 
 * @author <a href="mailto:rajni.kumari@synerzip.com">Rajni Kumari</a>
 */
public class JsonLiteSingleLinkFormatWriter extends JsonLiteFormatWriter<SingleLink> {
  private final String metadataType;

  public JsonLiteSingleLinkFormatWriter(String jsonpCallback, String metadataType) {
    super(jsonpCallback, metadataType);
    this.metadataType = metadataType;
  }

  @Override
  protected void writeContent(UriInfo uriInfo, JsonWriter jw, SingleLink link) {
    if (metadataType != null && !metadataType.equalsIgnoreCase(OdataJsonLiteConstant.METADATA_TYPE_NOMETADATA)) {
      jw.writeName(OdataJsonLiteConstant.METADATA_PROPERTY);
      jw.writeString(uriInfo.getBaseUri() + OdataJsonLiteConstant.METADATA_PROPERTY_WITH_HASH_DOLLAR + link.getSourceEntity().getEntitySetName() + "/$links/" + link.getTargetNavProp());
      jw.writeSeparator();
    }
    writeUri(jw, link);

  }

  static void writeUri(JsonWriter jw, SingleLink link) {
    /*{
       "odata.metadata": "http://.../Soupon.svc/$metadata#Users/$links/ReferredBy",
     "url": "http://.../Soupon.svc/Users('pilack')"
    }*/

    jw.writeName("url");
    jw.writeString(link.getUri());

  }

}
