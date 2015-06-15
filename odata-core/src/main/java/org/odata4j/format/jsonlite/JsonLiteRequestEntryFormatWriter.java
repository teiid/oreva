package org.odata4j.format.jsonlite;

import javax.ws.rs.core.UriInfo;

import org.odata4j.format.Entry;
import org.odata4j.format.json.JsonWriter;

/**
 * The Class JsonLiteRequestEntryFormatWriter.
 * 
 * @author <a href="mailto:rajni.kumari@synerzip.com">Rajni Kumari</a>
 */
public class JsonLiteRequestEntryFormatWriter extends JsonLiteFormatWriter<Entry> {
  private final String metadataType;
  
  public JsonLiteRequestEntryFormatWriter(String jsonpCallback, String metadataType) {
    super(jsonpCallback, metadataType);
    this.metadataType = metadataType;
  }

  @Override
  protected void writeContent(UriInfo uriInfo, JsonWriter jw, Entry target) {
    jw.writeName(OdataJsonLiteConstant.TYPE_PROPERTY);
    jw.writeString(target.getEntity().getType().getFullyQualifiedTypeName());
    jw.writeSeparator();
    writeOEntity(uriInfo, jw, target.getEntity(), target.getEntity().getEntitySet(), false);
  }

}
