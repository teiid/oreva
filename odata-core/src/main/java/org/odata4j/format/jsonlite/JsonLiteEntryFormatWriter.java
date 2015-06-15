package org.odata4j.format.jsonlite;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.odata4j.format.json.JsonWriter;
import org.odata4j.producer.EntityResponse;

/**
 * The Class JsonLiteEntryFormatWriter.
 * 
 * @author <a href="mailto:rajni.kumari@synerzip.com">Rajni Kumari</a>
 */
public class JsonLiteEntryFormatWriter extends JsonLiteFormatWriter<EntityResponse> {
  private final String ELEMENT_PROPERTY = "/@Element";
  private final String metadataType;

  public JsonLiteEntryFormatWriter(String jsonpCallback, String metadataType) {
    super(jsonpCallback, metadataType);
    this.metadataType = metadataType;
  }

  @Override
  protected void writeContent(UriInfo uriInfo, JsonWriter jw, EntityResponse target) {

    if (metadataType != null && !metadataType.equalsIgnoreCase(OdataJsonLiteConstant.METADATA_TYPE_NOMETADATA)) {
      jw.writeName(OdataJsonLiteConstant.METADATA_PROPERTY);
      String metadataInfo = uriInfo.getBaseUri().toString() + OdataJsonLiteConstant.METADATA_PROPERTY_WITH_HASH_DOLLAR + target.getEntity().getEntitySetName() + ELEMENT_PROPERTY;
      MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
      List<String> list = queryParameters.get("$select");
      if (list != null) {
        String select = "&$select=";
        //read the first value as it contains the property name selected with $select query option
        select = select + list.get(0);
        metadataInfo = metadataInfo + select;
      }
      jw.writeString(metadataInfo);
      jw.writeSeparator();
    }
    writeOEntity(uriInfo, jw, target.getEntity(), target.getEntity().getEntitySet(), true);

  }
}
