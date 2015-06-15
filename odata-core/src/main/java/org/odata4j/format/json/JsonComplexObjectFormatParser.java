package org.odata4j.format.json;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.odata4j.core.OCollection;
import org.odata4j.core.OComplexObject;
import org.odata4j.core.OComplexObjects;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OObject;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmProperty.CollectionKind;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.format.FormatParser;
import org.odata4j.format.Settings;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonParseException;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader.JsonEvent;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader.JsonStartPropertyEvent;

/**
 * Parser for OComplexObjects in JSON
 */
public class JsonComplexObjectFormatParser extends JsonFormatParser implements FormatParser<OComplexObject> {
  private static final boolean DUMP = false;

  private static void dump(String msg) {
    if (DUMP) System.out.println(msg);
  }

  public JsonComplexObjectFormatParser(Settings s) {
    super(s);
    returnType = (EdmComplexType) (s == null ? null : s.parseType);
  }

  public JsonComplexObjectFormatParser(EdmComplexType type) {
    super(null);
    returnType = type;
  }

  private EdmComplexType returnType = null;

  /*
   *the response for a complex type object in VJson format is like:
   * {"d":{"Address":{"__metadata":{"type":"ODataDemo.Address"},"Street":"NE 228th","City":"Sammamish","State":"WA","ZipCode":"98074","Country":"USA"}}} 
   */

  @Override
  public OComplexObject parse(Reader reader) {
    JsonStreamReader jsr = JsonStreamReaderFactory.createJsonStreamReader(reader);
    try {

      if (isResponse) {
        // the response object
        ensureNext(jsr);
        ensureStartObject(jsr.nextEvent());

        // "d" property
        ensureNext(jsr);
        ensureStartProperty(jsr.nextEvent(), DATA_PROPERTY);

        // the function object
        ensureNext(jsr);
        ensureStartObject(jsr.nextEvent());

        // "function" property
        ensureNext(jsr);
        ensureStartProperty(jsr.nextEvent());
      }

      // parse the entry, should start with startObject
      OComplexObject o = parseSingleObject(jsr);

      if (isResponse) {

        // the "d" property was our object...it is also a property.
        ensureNext(jsr);
        ensureEndProperty(jsr.nextEvent());

        ensureNext(jsr);
        ensureEndObject(jsr.nextEvent()); // the response object
      }

      return o;

    } finally {
      jsr.close();
    }
  }

  public OComplexObject parseSingleObject(JsonStreamReader jsr) {
    dump("json parseSingleObject: " + returnType.getFullyQualifiedTypeName());
    ensureNext(jsr);

    // this can be used in a context where we require an object and one
    // where there *may* be an object...like a collection
    OComplexObject result = null;
    JsonEvent event = jsr.nextEvent();
    if (event.isStartObject()) {
      List<OProperty<?>> props = new ArrayList<OProperty<?>>();
      result = eatProps(props, jsr);
    } // else not a start object.

    dump("json done parseSingleObject: " + returnType.getFullyQualifiedTypeName());
    return result;
  }

  public OComplexObject parseSingleObject(JsonStreamReader jsr, JsonEvent startPropertyEvent) {

    // the current JsonFormatParser implemenation, when parsing a complex object property value
    // has already eaten the startobject and the startproperty.

    List<OProperty<?>> props = new ArrayList<OProperty<?>>();
    addProperty(props, startPropertyEvent.asStartProperty().getName(), jsr);
    return eatProps(props, jsr);
  }
  
  /**
   * this method will skip the __metada part of a complex type instance as the verbose JSON object.
   * for the complext type double x, int y, string remark}, the .net entityCTInVJson is like:
   * {"__metadata":{"type":"UNITSContainer.testCType"},"remark":"test return complex type","x":1,"y":10}
   * @param jsr
   * @return
   */
  private JsonEvent skipVJson(JsonStreamReader jsr) {
    JsonEvent event = jsr.nextEvent();
    ensureStartObject(event); //StartObject('{')
    
    event = jsr.nextEvent();
    ensureStartProperty(event, "type"); //StartProperty(type)
    
    event = jsr.nextEvent();
    ensureEndProperty(event); //EndProperty(UNITSContainer.testCType)
    String complexTypeName = event.asEndProperty().getValue();
    
    event = jsr.nextEvent();
    ensureEndObject(event); //EndObject('}')
    
    event = jsr.nextEvent();
    if (event.isEndProperty()) {
      event = jsr.nextEvent();
    }

    return event;
  }

  private OComplexObject eatProps(List<OProperty<?>> props, JsonStreamReader jsr) {
    dump("json eatProps: " + returnType.getFullyQualifiedTypeName());
    ensureNext(jsr);
    while (jsr.hasNext()) {
      JsonEvent event = jsr.nextEvent();
      
      if (event.isStartProperty() && event.asStartProperty().getName().equals(JsonFormatParser.METADATA_PROPERTY)) {
        event = skipVJson(jsr);
      }

      if (event.isStartProperty()) {
        addProperty(props, event.asStartProperty().getName(), jsr);
      } else if (event.isEndObject()) {
        break;
      } else {
        throw new JsonParseException("unexpected parse event: " + event.toString());
      }
    }
    dump("json done eatProps: " + returnType.getFullyQualifiedTypeName());
    return OComplexObjects.create(returnType, props);
  }

  protected void addProperty(List<OProperty<?>> props, String name, JsonStreamReader jsr) {

    JsonEvent event = jsr.nextEvent();

    dump("json addProperty: " + name);
    if (event.isEndProperty()) {
      // scalar property
      EdmProperty ep = returnType.findProperty(name);

      if (ep == null) {
        throw new IllegalArgumentException("unknown property " + name + " for " + returnType.getFullyQualifiedTypeName());
      }

      if (!ep.getType().isSimple()) {
        if (event.asEndProperty().getValue() == null) {
          // a complex property can be null in which case it looks like a simple property
          if (ep.getType().getClass() == EdmCollectionType.class) {
              //collection type 
        	  props.add(OProperties.collection(name, (EdmCollectionType) ep.getType(), null));
          } else {
              //complex type 
        	  props.add(OProperties.complex(name, (EdmComplexType) ep.getType(), null));
          }
        } else {
          // we should not get here...
          throw new UnsupportedOperationException("Only simple properties supported");
        }
      } else {
        props.add(JsonTypeConverter.parse(name, (EdmSimpleType<?>) ep.getType(), event.asEndProperty().getValue(), event.asEndProperty().getValueTokenType()));
      }
    } else if (event.isStartObject()) {
      // embedded complex object or array.
      parseEmbedded(name, event, jsr, props);
    } else {
      throw new JsonParseException("expecting endproperty or startobject, got: " + event.toString());
    }
    dump("json done addProperty: " + name);
  }

  protected void parseEmbedded(String propName, JsonEvent event, JsonStreamReader jsr, List<OProperty<?>> props) {

    dump("json parseEmbedded " + propName);
    ensureStartObject(event);
    event = jsr.nextEvent();
    ensureStartProperty(event);
    JsonStartPropertyEvent startProp = event.asStartProperty();
    EdmProperty eprop = this.returnType.findProperty(propName);
    
    if (startProp.getName().equals(JsonFormatParser.METADATA_PROPERTY)) {
      event = skipVJson(jsr);
      ensureStartProperty(event);
      startProp = event.asStartProperty();
    }
    
    if (RESULTS_PROPERTY.equals(startProp.getName())) {
      // embedded collection

      dump("json embeddedCollection" + (eprop != null ? eprop.getName() : "null"));
      if (eprop != null) {
        EdmCollectionType collectionType = null;
        // for some reason, the odata4j will define collection property as: (not in spec)
        // <Property CollectionKind="Collection" Name="EmbeddedCollectionString" Nullable="true" Type="Edm.String" />
        // which will cause the getType() return the element type, not the collection type.
        if (eprop.getCollectionKind() != null && eprop.getCollectionKind() != CollectionKind.NONE) {
          collectionType = new EdmCollectionType(eprop.getCollectionKind(), eprop.getType());
        } else {
          // handle collection property is defined as Collection(type)
          collectionType = (EdmCollectionType) eprop.getType();
        }
        JsonCollectionFormatParser cfp = new JsonCollectionFormatParser(collectionType, this.metadata);
        OCollection<? extends OObject> collection = cfp.parseCollection(jsr);
        ensureEndArray(jsr.previousEvent());
        props.add(OProperties.collection(propName, collectionType, collection));
        ensureEndProperty(jsr.nextEvent()); // embedded "results" property
        ensureEndObject(jsr.nextEvent()); // the collection object
        ensureEndProperty(jsr.nextEvent()); // propName
      } else {
        throw new RuntimeException("unhandled property: " + startProp.getName());
      }
      dump("json done embeddedCollection" + (eprop != null ? eprop.getName() : "null"));

    } else if (eprop.getType() instanceof EdmComplexType) {
      // a "regular property", must be an embedded complex object
      EdmComplexType outerType = this.returnType;
      this.returnType = (EdmComplexType) eprop.getType();
      dump("json embedded complex" + returnType.getFullyQualifiedTypeName());
      try {
        OComplexObject o = this.parseSingleObject(jsr, startProp);
        ensureEndObject(jsr.previousEvent());
        props.add(OProperties.complex(propName, this.returnType, o.getProperties()));
        ensureEndProperty(jsr.nextEvent());
      } finally {
        this.returnType = outerType;
      }
      dump("json done embedded complex" + returnType.getFullyQualifiedTypeName());
    } else {
      throw new RuntimeException("unhandled property: " + startProp.getName());
    }
    dump("json done parseEmbedded " + propName);
  }

}
