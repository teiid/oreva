package org.odata4j.format.jsonlite;

import java.io.Writer;

import javax.ws.rs.core.UriInfo;

import org.odata4j.core.OCollection;
import org.odata4j.core.OComplexObject;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OObject;
import org.odata4j.core.OSimpleObject;
import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmProperty.CollectionKind;
import org.odata4j.format.Parameters;
import org.odata4j.format.json.JsonWriter;

/**
 * The Class JsonLiteParametersFormatWriter.
 * 
 * @author <a href="mailto:rajni.kumari@synerzip.com">Rajni Kumari</a>
 */
public class JsonLiteParametersFormatWriter extends JsonLiteFormatWriter<Parameters> {

  private final String metadataType;
  
  /**
   * Instantiates a new json lite parameters format writer.
   *
   * @param jsonpCallback the jsonp callback
   * @param metadataType 
   */
  public JsonLiteParametersFormatWriter(String jsonpCallback, String metadataType) {
    super(jsonpCallback, metadataType);
    this.metadataType= metadataType;
  }

  @Override
  protected void writeContent(UriInfo uriInfo, JsonWriter jw, Parameters parameters) {

    boolean isFirst = true;
    jw.startObject();
    for (OFunctionParameter param : parameters.getParameters()) {
      if (isFirst) {
        isFirst = false;
      } else {
        jw.writeSeparator();
      }
      jw.writeName(param.getName());
      OObject oo = param.getValue();
      if (oo instanceof OCollection) {
        EdmCollectionType colType = new EdmCollectionType(CollectionKind.Collection, param.getType());
        writeValue(jw, colType, oo, false);
      } else if (param.getType().isSimple()) {
        writeValue(jw, param.getType(), ((OSimpleObject<?>) oo).getValue(), false);
      } else if (oo instanceof OComplexObject) {
        EdmComplexType complexType = (EdmComplexType) param.getType();
        writeValue(jw, complexType, oo, false);
      }
      else {
        writeValue(jw, param.getType(), param.getValue(), false);
      }
    }
    jw.endObject();
  }

  @Override
  public void write(UriInfo uriInfo, Writer w, Parameters target) {

    JsonWriter jw = new JsonWriter(w);
    if (getJsonpCallback() != null) {
      jw.startCallback(getJsonpCallback());
    }

    writeContent(uriInfo, jw, target);
  }

}
