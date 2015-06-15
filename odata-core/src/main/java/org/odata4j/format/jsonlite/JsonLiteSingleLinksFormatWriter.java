package org.odata4j.format.jsonlite;

import javax.ws.rs.core.UriInfo;

import org.odata4j.format.SingleLink;
import org.odata4j.format.SingleLinks;
import org.odata4j.format.json.JsonWriter;

/**
 * The Class JsonLiteSingleLinksFormatWriter.
 * 
 * @author <a href="mailto:rajni.kumari@synerzip.com">Rajni Kumari</a>
 */
public class JsonLiteSingleLinksFormatWriter extends JsonLiteFormatWriter<SingleLinks> {
  private final String metadataType;

  public JsonLiteSingleLinksFormatWriter(String jsonpCallback, String metadataType) {
    super(jsonpCallback, metadataType);
    this.metadataType = metadataType;
  }

  @Override
  protected void writeContent(UriInfo uriInfo, JsonWriter jw, SingleLinks links) {
    if (metadataType != null && !metadataType.equalsIgnoreCase(OdataJsonLiteConstant.METADATA_TYPE_NOMETADATA)) {
      jw.writeName(OdataJsonLiteConstant.METADATA_PROPERTY);
      jw.writeString(uriInfo.getBaseUri() + OdataJsonLiteConstant.METADATA_PROPERTY_WITH_HASH_DOLLAR + links.getSourceEntity().getEntitySetName() + "/$links/" + links.getTargetNavProp());
      jw.writeSeparator();
    }
    jw.writeName(OdataJsonLiteConstant.VALUE_PROPERTY);
    jw.startArray();
    boolean isFirst = true;
    for (SingleLink link : links) {
      if (!isFirst)
        jw.writeSeparator();
      else
        isFirst = false;
      jw.startObject();
      JsonLiteSingleLinkFormatWriter.writeUri(jw, link);
      jw.endObject();
    }
    jw.endArray();

  }
}
