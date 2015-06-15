package org.odata4j.format.jsonlite;

import java.io.Reader;

import org.odata4j.core.OCollection;
import org.odata4j.core.OCollections;
import org.odata4j.core.OComplexObject;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OEntity;
import org.odata4j.core.OObject;
import org.odata4j.core.OSimpleObjects;
import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmType;
import org.odata4j.exceptions.NotImplementedException;
import org.odata4j.format.Entry;
import org.odata4j.format.FormatParser;
import org.odata4j.format.FormatParserFactory;
import org.odata4j.format.FormatType;
import org.odata4j.format.Settings;
import org.odata4j.format.json.JsonEntityFormatParser;
import org.odata4j.format.json.JsonStreamReaderFactory;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader.JsonEvent;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader.JsonValueEvent;
import org.odata4j.format.jsonlite.JsonLiteFeedFormatParser.JsonFeed;

/**
 * Handles the paring of an OCollection in JSON-LITE format.
 *
 * @author <a href="mailto:shantanu@synerzip.com">Shantanu Dindokar</a>
 * 
 */
public class JsonLiteCollectionFormatParser extends JsonLiteFormatParser implements FormatParser<OCollection<? extends OObject>> {

  private final EdmCollectionType returnType;

  /**
   * Instantiates a new json-lite collection format parser.
   *
   * @param settings the Settings
   */
  public JsonLiteCollectionFormatParser(Settings settings) {
    super(settings);
    returnType = (EdmCollectionType) (settings == null ? null : settings.parseType);
  }

  /**
   * Instantiates a new json-lite collection format parser.
   *
   * @param collectionType the EdmCollectionType
   * @param metadata the EdmDataServices
   */
  public JsonLiteCollectionFormatParser(EdmCollectionType collectionType, EdmDataServices metadata) {
    super(null);
    this.metadata = metadata;
    returnType = collectionType;
  }

  /* (non-Javadoc)
   * @see org.odata4j.format.FormatParser#parse(java.io.Reader)
   * 
   * 
   */
  @Override
  public OCollection<? extends OObject> parse(Reader reader) {

    if (this.returnType.getItemType().getClass().isAssignableFrom(EdmEntityType.class)) {
      return parseFunctionFeed(reader);
    }

    JsonStreamReader jsr = JsonStreamReaderFactory.createJsonStreamReader(reader);
    try {
      if (isResponse) {
        ensureNext(jsr);
        ensureStartObject(jsr.nextEvent()); // the response object

        // "odata.metadata" property
        JsonEvent nextEvent = jsr.nextEvent();
        if (nextEvent.isStartProperty() && nextEvent.asStartProperty().getName().equalsIgnoreCase(OdataJsonLiteConstant.VALUE_PROPERTY)) {

        } else {
          ensureNext(jsr);
          ensureStartProperty(nextEvent, OdataJsonLiteConstant.METADATA_PROPERTY);
          ensureEndProperty(jsr.nextEvent());
          ensureNext(jsr);
          ensureStartProperty(jsr.nextEvent(), OdataJsonLiteConstant.VALUE_PROPERTY);
        }
      }
      // parse the entry
      OCollection<? extends OObject> o = parseCollection(jsr, jsr.nextEvent());
      if (isResponse) {
        ensureEndProperty(jsr.nextEvent());
        ensureEndObject(jsr.nextEvent());
      }

      return o;

    } finally {
      jsr.close();
    }
  }

  /**
   * Function parsing will be handled in separate story. 
   *
   * @param reader the reader
   * @return the o collection<? extends o object>
   */
  protected OCollection<? extends OObject> parseFunctionFeed(Reader reader) {
    EdmEntitySet entitySet = this.metadata.getEdmEntitySet((EdmEntityType) returnType.getItemType());

    Settings settings = new Settings(
        // someone really needs to spend some time on service version negotiation....
        ODataVersion.V3,
        this.metadata,
        entitySet.getName(),
        this.entityKey,
        null, // feed customization mapping
        this.isResponse,
        this.returnType.getItemType());

    JsonLiteFeedFormatParser parser = new JsonLiteFeedFormatParser(settings);
    JsonFeed feed = parser.parse(reader);
    OCollection.Builder<OObject> c = newCollectionBuilder();
    for (Entry e : feed.getEntries()) {
      c.add(e.getEntity());
    }
    return c.build();

    //return null;
  }

  /**
   * We make this public so that the JsonLiteParametersFormatParser can use it to read 
   * parameter whose type is collection.
   * While instantiating JsonLiteCollectionFormatParser from JsonLiteParametersFormatParser
   * should set odata version, metadata, entitySetName, entityKey in the settings.
   *  
   * @param jsr the json stream reader
   * @param event the JsonEvent
   * @return the collection object
   */
  public OCollection<? extends OObject> parseCollection(JsonStreamReader jsr, JsonEvent event) {

    ensureStartArray(event);

    OCollection.Builder<OObject> c = newCollectionBuilder();

    if (this.returnType.getItemType().isSimple()) {
      parseCollectionOfSimpleTypes(c, jsr);
    }
    else
    {
      FormatParser<? extends OObject> parser = createItemParser(this.returnType.getItemType());
      while (jsr.hasNext()) {

        if (parser instanceof JsonLiteComplexObjectFormatParser) {
          OComplexObject obj = ((JsonLiteComplexObjectFormatParser) parser).parseSingleObject(jsr, jsr.nextEvent());
          // null if not there
          if (obj != null) {
            c = c.add(obj);
          } else {
            break;
          }
        } else if (parser instanceof JsonEntityFormatParser) {
          OEntity obj = ((JsonEntityFormatParser) parser).parseSingleEntity(jsr);
          if (obj != null) {
            c = c.add(obj);
          } else {
            break;
          }
        }
        else {
          throw new NotImplementedException("collections of type: " + this.returnType.getItemType().getFullyQualifiedTypeName() + " not implemented");
        }
      }
    }

    // we should see the end of the array
    ensureEndArray(jsr.previousEvent());
    return c.build();
  }

  /**
   * Parses the collection of simple type.
   *
   * @param builder the builder
   * @param jsr the json stream reader
   */
  protected void parseCollectionOfSimpleTypes(OCollection.Builder<OObject> builder, JsonStreamReader jsr) {
    while (jsr.hasNext()) {
      JsonEvent e = jsr.nextEvent();
      if (e.isValue()) {
        JsonValueEvent ve = e.asValue();
        builder.add(OSimpleObjects.parse((EdmSimpleType<?>) this.returnType.getItemType(), ve.getValue()));
      } else if (e.isEndArray()) {
        break;
      } else {
        throw new RuntimeException("invalid JSON content");
      }
    }
  }

  protected OCollection.Builder<OObject> newCollectionBuilder() {
    return OCollections.<OObject> newBuilder(this.returnType.getItemType());
  }

  /**
   * Provides the appropriate parser depending on the edmtype.
   *
   * @param edmType the edm type
   * @return the format parser<? extends o object>
   */
  protected FormatParser<? extends OObject> createItemParser(EdmType edmType) {
    // each item is parsed as a standalone item, not a response item
    Settings s = new Settings(
        this.version,
        this.metadata,
        this.entitySetName,
        this.entityKey,
        null, // FeedCustomizationMapping fcMapping,
        false, // boolean isResponse);
        edmType); // expected type

    return FormatParserFactory.getParser(EdmType.getInstanceType(edmType), FormatType.JSON, s);
  }
}
