/**
 * 
 */
package org.odata4j.format.jsonlite;

import javax.ws.rs.core.UriInfo;

import org.odata4j.core.OCollection;
import org.odata4j.core.OEntity;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmType;
import org.odata4j.format.json.JsonWriter;
import org.odata4j.producer.CollectionResponse;

/**
 * Writer for OCollections in JSON-LITE
 *  
 * @author <a href="mailto:rajni.kumari@synerzip.com">Rajni Kumari</a>
 *
 */
public class JsonLiteCollectionFormatWriter extends JsonLiteFormatWriter<CollectionResponse<?>> {

  /** The Constant collectionProperty. Used in metadata */
  private static final String collectionProperty = "Collection";

  private final String metadataType;

  /**
   * Instantiates a new json lite collection format writer.
   *
   * @param jsonpCallback the jsonp callback
   * @param metadataType 
   */
  public JsonLiteCollectionFormatWriter(String jsonpCallback, String metadataType) {
    super(jsonpCallback, metadataType);
    this.metadataType = metadataType;
  }

  @Override
  protected void writeContent(UriInfo uriInfo, JsonWriter jw, CollectionResponse<?> target) {
    //"odata.metadata": "http://.../Soupon.svc/$metadata#Collection(Edm.String)",   "value": ["gazpacho", "tomato", "vegetarian"]
    OCollection<?> c = target.getCollection();
    EdmType ctype = c.getType();
    if (metadataType != null && !metadataType.equalsIgnoreCase(OdataJsonLiteConstant.METADATA_TYPE_NOMETADATA)) {
      jw.writeName(OdataJsonLiteConstant.METADATA_PROPERTY);
      jw.writeString(uriInfo.getBaseUri() + OdataJsonLiteConstant.METADATA_PROPERTY_WITH_HASH_DOLLAR + collectionProperty + "(" + ctype.getFullyQualifiedTypeName() + ")");
      jw.writeSeparator();
    }
    jw.writeName(OdataJsonLiteConstant.VALUE_PROPERTY);
    jw.startArray();
    {
      boolean isFirst = true;
      for (Object o : c) {
        if (!isFirst) {
          jw.writeSeparator();
        }
        else {
          isFirst = false;
        }
        if (ctype instanceof EdmEntityType) {
          OEntity entity = (OEntity) o;
          super.writeOEntity(uriInfo, jw, entity, entity.getEntitySet(), true); // its a response.
        } else {
          super.writeValue(jw, ctype, o, true);
        }
      }
    }
    jw.endArray();

  }
}
