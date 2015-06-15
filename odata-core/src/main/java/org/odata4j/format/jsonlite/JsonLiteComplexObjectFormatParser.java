package org.odata4j.format.jsonlite;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.odata4j.core.OComplexObject;
import org.odata4j.core.OComplexObjects;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmType;
import org.odata4j.format.FormatParser;
import org.odata4j.format.Settings;
import org.odata4j.format.json.JsonStreamReaderFactory;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonParseException;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader.JsonEvent;
import org.odata4j.format.json.JsonTypeConverter;

/**
 * Parser for OComplexObjects in JSON-LITE
 * 
 * @author <a href="mailto:shantanu@synerzip.com">Shantanu Dindokar</a>
 */
public class JsonLiteComplexObjectFormatParser extends JsonLiteFormatParser implements FormatParser<OComplexObject> {

  /** EdmComplexType */
  private EdmComplexType returnType = null;
  private boolean nometadata;
  private String metadataType;

  /**
   * Instantiates a new json lite complex object format parser.
   *
   * @param settings the Settings
   */
  public JsonLiteComplexObjectFormatParser(Settings settings) {
    super(settings);
    returnType = (EdmComplexType) (settings == null ? null : settings.parseType);
  }

  /**
   * Instantiates a new json lite complex object format parser.
   *
   * @param type the EdmComplexType
   */
  public JsonLiteComplexObjectFormatParser(EdmComplexType type) {
    super(null);
    returnType = type;
  }

  public JsonLiteComplexObjectFormatParser(Settings settings, String metadataType) {
    super(settings);
    returnType = (EdmComplexType) (settings == null ? null : settings.parseType);
    this.metadataType = metadataType;
  }

  /* (non-Javadoc)
   * @see org.odata4j.format.FormatParser#parse(java.io.Reader)
   */
  @Override
  public OComplexObject parse(Reader reader) {
    JsonStreamReader jsr = JsonStreamReaderFactory.createJsonStreamReader(reader);
    JsonEvent nextEevent = null;
    try {

      if (isResponse) {
        // the response object
        ensureNext(jsr);
        ensureStartObject(jsr.nextEvent());

        // "odata.metadata" property
        ensureNext(jsr);
        if (!metadataType.equalsIgnoreCase(OdataJsonLiteConstant.METADATA_TYPE_NOMETADATA)) {
          nextEevent = jsr.nextEvent();
          ensureStartProperty(nextEevent, OdataJsonLiteConstant.METADATA_PROPERTY);
          ensureEndProperty(jsr.nextEvent());
        }

      }

      // parse the entry, should start with startObject
      List<OProperty<?>> props = new ArrayList<OProperty<?>>();
      OComplexObject o = addProps(props, jsr);
      return o;

    } finally {
      jsr.close();
    }
  }

  /**
   * Parses the single object.
   *
   * @param jsr the JsonStreamReader
   * @param event the JsonEvent
   * @return the complex object
   */
  public OComplexObject parseSingleObject(JsonStreamReader jsr, JsonEvent event) {
    ensureNext(jsr);
    // this can be used in a context where we require an object and one
    // where there *may* be an object...like a collection
    OComplexObject result = null;
    if (event.isStartObject()) {
      List<OProperty<?>> props = new ArrayList<OProperty<?>>();
      result = addProps(props, jsr);
    } // else not a start object.
    return result;
  }

  /**
   * API which adds properties to ComplexObject.
   *
   * @param props the List of OProperty
   * @param jsr the JsonStreamReader
   * @return the complex object
   */
  private OComplexObject addProps(List<OProperty<?>> props, JsonStreamReader jsr) {

    ensureNext(jsr);
    while (jsr.hasNext()) {
      JsonEvent event = jsr.nextEvent();
      if (event.isStartProperty() && !(event.asStartProperty().getName().contains(OdataJsonLiteConstant.TYPE_PROPERTY))) {
        addProperty(props, event.asStartProperty().getName(), jsr);
      } else if (event.isEndObject()) {
        break;
      }
    }
    return OComplexObjects.create(returnType, props);
  }

  /**
   * Processes and adds the individual property to ComplexObject depending on what type of
   * property it is say collection or nested complex objects.
   *
   * @param props the props
   * @param name the String propertyName
   * @param jsr the JsonStreamReader
   */
  protected void addProperty(List<OProperty<?>> props, String propertyName, JsonStreamReader jsr) {
    JsonEvent event = jsr.nextEvent();
    if (event.isEndProperty()) {
      // scalar property   
      EdmProperty ep = returnType.findProperty(propertyName);
      if (ep == null) {
        throw new IllegalArgumentException("unknown property " + propertyName + " for " + returnType.getFullyQualifiedTypeName());
      }

      if (!ep.getType().isSimple()) {
        if (event.asEndProperty().getValue() == null) {
          // a complex property can be null in which case it looks like a simple property
          if (ep.getType().getClass() == EdmCollectionType.class) {
            //collection type 
            props.add(OProperties.collection(propertyName, (EdmCollectionType) ep.getType(), null));
          } else {
            //complex type 
            props.add(OProperties.complex(propertyName, (EdmComplexType) ep.getType(), null));
          }
        } else {
          throw new UnsupportedOperationException("Only simple properties supported");
        }
      } else {
        props.add(JsonTypeConverter.parse(propertyName, (EdmSimpleType<?>) ep.getType(), event.asEndProperty().getValue(), event.asEndProperty().getValueTokenType()));
      }
    } else if (event.isStartArray()) {
      JsonObjectPropertyValue rt = new JsonObjectPropertyValue();
      EdmProperty eprop = returnType.findProperty(propertyName);
      EdmType propType = this.getPropertyType(eprop);
      if (propType instanceof EdmCollectionType) {
        rt.collectionType = (EdmCollectionType) propType;
        JsonLiteCollectionFormatParser cfp = new JsonLiteCollectionFormatParser(rt.collectionType, this.metadata);
        rt.collection = cfp.parseCollection(jsr, event);
      }
      props.add(OProperties.collection(propertyName, rt.collectionType, rt.collection));
    } else if (event.isStartObject()) {
      //handle nested complex objects
      JsonObjectPropertyValue rt = new JsonObjectPropertyValue();
      EdmProperty eprop = returnType.findProperty(propertyName);
      EdmType propType = this.getPropertyType(eprop);
      if (propType instanceof EdmComplexType) {
        EdmComplexType complextype = this.metadata.findEdmComplexType(((EdmComplexType) propType).getFullyQualifiedTypeName());
        JsonLiteComplexObjectFormatParser cmp = new JsonLiteComplexObjectFormatParser(complextype);
        rt.complexObject = cmp.parseSingleObject(jsr, event);
      }
      props.add(OProperties.complex(propertyName, (EdmComplexType) rt.complexObject.getType(),
          rt.complexObject.getProperties()));
    } else {
      throw new JsonParseException("expecting endproperty or startobject, got: " + event.toString());
    }
  }
}
