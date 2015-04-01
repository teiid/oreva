package org.odata4j.format.jsonlite;

import javax.ws.rs.core.UriInfo;

import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.format.json.JsonWriter;

/**
 * The Class JsonLiteServiceDocumentFormatWriter.
 * 
 * @author <a href="mailto:rajni.kumari@synerzip.com">Rajni Kumari</a>
 */
public class JsonLiteServiceDocumentFormatWriter extends JsonLiteFormatWriter<EdmDataServices> {
  private final String METADATA_PROPERTY_WITH_DOLLAR = "$metadata";
  private final String NAME_PROPERTY = "name";
  private final String URL_PROPERTY = "url";
  private final String metadataType;
  

  public JsonLiteServiceDocumentFormatWriter(String jsonpCallback, String metadataType) {
    super(jsonpCallback, metadataType);
    this.metadataType = metadataType;

  }

  @Override
  public void writeContent(UriInfo uriInfo, JsonWriter jw, EdmDataServices target) {
    if (metadataType != null && !metadataType.equalsIgnoreCase(OdataJsonLiteConstant.METADATA_TYPE_NOMETADATA)) {
      jw.writeName(OdataJsonLiteConstant.METADATA_PROPERTY);
      jw.writeString(uriInfo.getBaseUri() + METADATA_PROPERTY_WITH_DOLLAR);
      jw.writeSeparator();
    }
    jw.writeName(OdataJsonLiteConstant.VALUE_PROPERTY);
    jw.startArray();

    {
      boolean isFirst = true;

      for (EdmEntitySet ees : target.getEntitySets()) {
        if (isFirst) {
          jw.startObject();
          isFirst = false;
        }
        else {
          jw.writeSeparator();
          jw.startObject();
        }

        jw.writeName(NAME_PROPERTY);
        jw.writeString(ees.getName());
        jw.writeSeparator();
        jw.writeName(URL_PROPERTY);
        jw.writeString(ees.getName());
        jw.endObject();
      }

    }
    jw.endArray();
  }
}
