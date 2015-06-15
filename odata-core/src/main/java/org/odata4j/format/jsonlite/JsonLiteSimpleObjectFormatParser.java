package org.odata4j.format.jsonlite;

import java.io.Reader;

import org.odata4j.core.OSimpleObject;
import org.odata4j.core.OSimpleObjects;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.format.FormatParser;
import org.odata4j.format.Settings;
import org.odata4j.format.json.JsonStreamReaderFactory;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader.JsonEvent;

/**
 * parses a response from a service operation that returns EdmSimpleType
 * 
 * @author <a href="mailto:shantanu@synerzip.com">Shantanu Dindokar</a>
 */
public class JsonLiteSimpleObjectFormatParser extends JsonLiteFormatParser implements FormatParser<OSimpleObject<?>> {

  public JsonLiteSimpleObjectFormatParser(Settings settings) {
    super(settings);
  }

  @Override
  public OSimpleObject<?> parse(Reader reader) {
    JsonStreamReaderFactory.JsonStreamReader jsr = JsonStreamReaderFactory.createJsonStreamReader(reader);
    JsonEvent endProp;
    ensureNext(jsr);
    ensureStartObject(jsr.nextEvent()); // the response object
    // "odata.metadata"  
    ensureNext(jsr);
    JsonEvent event = jsr.nextEvent();
    if (event.asStartProperty().getName().equalsIgnoreCase(OdataJsonLiteConstant.METADATA_PROPERTY)) {
      ensureStartProperty(event, OdataJsonLiteConstant.METADATA_PROPERTY);
      ensureEndProperty(jsr.nextEvent());
      //values
      ensureNext(jsr);
      ensureStartProperty(jsr.nextEvent());
      endProp = jsr.nextEvent();
      ensureEndProperty(endProp);
      ensureEndObject(jsr.nextEvent());
    } else {
      ensureStartProperty(event);
      endProp = jsr.nextEvent();
      ensureEndProperty(endProp);
      ensureEndObject(jsr.nextEvent());

    }
    return OSimpleObjects.parse((EdmSimpleType<?>) this.parseType, endProp.asEndProperty().getValue());
  }

}
