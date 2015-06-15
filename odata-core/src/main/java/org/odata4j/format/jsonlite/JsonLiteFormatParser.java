package org.odata4j.format.jsonlite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.odata4j.core.OBindableEntities;
import org.odata4j.core.OBindableEntity;
import org.odata4j.core.OCollection;
import org.odata4j.core.OComplexObject;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OLink;
import org.odata4j.core.OLinks;
import org.odata4j.core.ONamedStreamLink;
import org.odata4j.core.OObject;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.core.StreamEntity;
import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmFunctionImport.FunctionKind;
import org.odata4j.edm.EdmMultiplicity;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmProperty.CollectionKind;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmType;
import org.odata4j.format.Entry;
import org.odata4j.format.Settings;
import org.odata4j.format.json.JsonFormatParser;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader.JsonEvent;
import org.odata4j.format.json.JsonTypeConverter;
import org.odata4j.format.jsonlite.JsonLiteFeedFormatParser.JsonEntry;
import org.odata4j.format.jsonlite.JsonLiteFeedFormatParser.JsonFeed;
import org.odata4j.urlencoder.ConversionUtil;

/**
  * The Class JsonEntryMetaData.
  * 
  * @author <a href="mailto:shantanu@synerzip.com">Shantanu Dindokar</a>
  */
public class JsonLiteFormatParser {

  static class JsonEntryMetaData {
    String uri;
    String type;
    String etag;
    Map<String, EdmFunctionImport> actions = new HashMap<String, EdmFunctionImport>();
    Map<String, EdmFunctionImport> functions = new HashMap<String, EdmFunctionImport>();
  }

  static class JsonObjectPropertyValue {
    String uri;
    OEntity entity;
    List<OEntity> entities;
    OCollection<? extends OObject> collection;
    EdmCollectionType collectionType;
    OComplexObject complexObject;
  }

  protected ODataVersion version;
  protected EdmDataServices metadata;
  protected String entitySetName;
  protected OEntityKey entityKey;
  protected boolean isResponse;
  protected EdmType parseType;
  protected EdmFunctionImport parseFunction;
  private int entityKeyCounter = 0;

  protected JsonLiteFormatParser(Settings settings) {
    this.version = settings == null ? null : settings.version;
    this.metadata = settings == null ? null : settings.metadata;
    this.entitySetName = settings == null ? null : settings.entitySetName;
    this.entityKey = settings == null ? null : settings.entityKey;
    this.isResponse = settings == null ? false : settings.isResponse;
    this.parseType = settings == null ? null : settings.parseType;
    this.parseFunction = settings == null ? null : settings.parseFunction;
  }

  protected JsonFeed parseFeed(EdmEntitySet ees, JsonStreamReader jsr) {
    JsonFeed feed = new JsonFeed();
    feed.entries = new ArrayList<Entry>();

    while (jsr.hasNext()) {
      JsonEvent event = jsr.nextEvent();

      if (event.isStartObject()) {
        JsonEntry entry = parseEntry(ees, jsr);
        feed.entries.add(entry);
      } else if (event.isEndArray()) {
        break;
      }
    }

    return feed;
  }

  protected void resolveEntityType(JsonEntry entry) {
    // does the metadata refine the type of entity to expect?
    if (entry.jemd != null && entry.jemd.type != null && !entry.getEntitySet().getType().getFullyQualifiedTypeName().equals(entry.jemd.type)) {
      // yes it does.

      entry.setEntityType((EdmEntityType) this.metadata.findEdmEntityType(entry.jemd.type));
      if (entry.getEntityType() == null) {
        throw new IllegalArgumentException("failed resolving type: " + entry.jemd.type);
      }
    } else {
      entry.setEntityType(entry.getEntitySet().getType());
    }
  }

  protected JsonEntry parseEntry(JsonEntryMetaData jemd, EdmEntitySet ees, JsonStreamReader jsr) {
    JsonEntry entry = new JsonEntry(ees, jemd);
    JsonObjectPropertyValue rt = new JsonObjectPropertyValue();
    if (jemd != null)
      resolveEntityType(entry);
    entry.properties = new ArrayList<OProperty<?>>();
    //start to generate uri
    entry.jemd = new JsonEntryMetaData();
    entry.jemd.uri = OdataJsonLiteConstant.START_URI;
    entry.links = new ArrayList<OLink>();
    List<Object> mediaList = new ArrayList<Object>();
    StreamEntity streamEntity = new StreamEntity();
    List<ONamedStreamLink> nsLinks = new ArrayList<ONamedStreamLink>();
    Map<String, String> nsLinksContentType = new HashMap<String, String>();
    while (jsr.hasNext()) {
      JsonEvent event = jsr.nextEvent();

      if (event.isStartProperty() && event.asStartProperty().getName().equals(OdataJsonLiteConstant.TYPE_PROPERTY)) {
        ensureEndProperty(event = jsr.nextEvent());
        entry.jemd.type = event.asEndProperty().getValue();
        this.resolveEntityType(entry);
        continue;
      }
      // build  extension for stream entity
      if (event.isStartProperty() && event.asStartProperty().getName().equals(OdataJsonLiteConstant.MEDIA_READ_LINK_PROPERTY)) {
        if (mediaList.isEmpty()) {
          mediaList.add(streamEntity);
        }
        event = jsr.nextEvent();
        streamEntity.setAtomEntitySource(event.asEndProperty().getValue());
      } else if (event.isStartProperty() && event.asStartProperty().getName().equals(OdataJsonLiteConstant.MEDIA_CONTENT_TYPE_PROPERTY)) {
        if (mediaList.isEmpty()) {
          mediaList.add(streamEntity);
        }
        event = jsr.nextEvent();
        streamEntity.setAtomEntityType(event.asEndProperty().getValue());
      } else if (event.isStartProperty() && event.asStartProperty().getName().endsWith("@" + OdataJsonLiteConstant.MEDIA_EDIT_LINK_PROPERTY)) {
        String propName = event.asStartProperty().getName();
        String nsName = propName.substring(0, propName.length() - OdataJsonLiteConstant.MEDIA_EDIT_LINK_PROPERTY.length() - 1);
        event = jsr.nextEvent();
        ONamedStreamLink nsLink = OLinks.namedStreamLink(JsonFormatParser.JSON_NAMED_STREAM_EDIT_MEDIA + nsName, nsName, event.asEndProperty().getValue(), null);
        nsLinks.add(nsLink);
      } else if (event.isStartProperty() && event.asStartProperty().getName().endsWith("@" + OdataJsonLiteConstant.MEDIA_READ_LINK_PROPERTY)) {
        String propName = event.asStartProperty().getName();
        String nsName = propName.substring(0, propName.length() - OdataJsonLiteConstant.MEDIA_READ_LINK_PROPERTY.length() - 1);
        event = jsr.nextEvent();
        ONamedStreamLink nsLink = OLinks.namedStreamLink(JsonFormatParser.JSON_NAMED_STREAM_SRC_MEDIA + nsName, nsName, event.asEndProperty().getValue(), null);
        nsLinks.add(nsLink);
      } else if (event.isStartProperty() && event.asStartProperty().getName().endsWith("@" + OdataJsonLiteConstant.MEDIA_CONTENT_TYPE_PROPERTY)) {
        String propName = event.asStartProperty().getName();
        String nsName = propName.substring(0, propName.length() - OdataJsonLiteConstant.MEDIA_CONTENT_TYPE_PROPERTY.length() - 1);
        event = jsr.nextEvent();
        nsLinksContentType.put(nsName, event.asEndProperty().getValue());
      } else if (event.isStartProperty() && !(event.asStartProperty().getName().contains(OdataJsonLiteConstant.TYPE_PROPERTY))
          && !(event.asStartProperty().getName().contains(OdataJsonLiteConstant.METADATA_PROPERTY))
          && !(event.asStartProperty().getName().contains(OdataJsonLiteConstant.MEDIA_READ_LINK_PROPERTY))
          && !(event.asStartProperty().getName().contains(OdataJsonLiteConstant.MEDIA_EDIT_LINK_PROPERTY))
          && !(event.asStartProperty().getName().contains(OdataJsonLiteConstant.MEDIA_CONTENT_TYPE_PROPERTY))
          && !(event.asStartProperty().getName().contains(OdataJsonLiteConstant.EDIT_LINK_PROPERTY))
          && !(event.asStartProperty().getName().contains(OdataJsonLiteConstant.ASSOCIATION_LINK_URL_PROPERTY))) {
        if (event.isStartProperty() && event.asStartProperty().getName().contains(OdataJsonLiteConstant.ID_PROPERTY)) {
          event = jsr.nextEvent();
          String uri = event.asEndProperty().getValue();
          rt.uri = uri;
        } else {
          addProperty(entry, ees, event.asStartProperty().getName(), jsr, rt);
        }
      } else if (event.isEndObject()) {
        break;
      }
    }
    OBindableEntity bindableExtension = null;
    if (entry.jemd != null && (entry.jemd.actions.size() > 0 || entry.jemd.functions.size() > 0)) {
      bindableExtension = OBindableEntities.createBindableExtension(entry.jemd.actions, entry.jemd.functions);
    }
    if (mediaList.isEmpty()) {
      entry.oentity = toOEntity(ees, entry.getEntityType(), entry.getEntityKey(), entry.getETag(), entry.properties, entry.links, bindableExtension);
    } else {
      entry.oentity = toOEntity(ees, entry.getEntityType(), entry.getEntityKey(), entry.getETag(), entry.properties, entry.links, bindableExtension, mediaList.get(0));
    }

    // now set the NamedStreamLink's content type.
    for (ONamedStreamLink nsLink : nsLinks) {
      String name = nsLink.getTitle();
      entry.links.add(OLinks.namedStreamLink(nsLink.getRelation(), nsLink.getTitle(), nsLink.getHref(), nsLinksContentType.get(name)));
    }
    entityKeyCounter = 0;
    return entry;
  }

  protected JsonEntry parseEntry(EdmEntitySet ees, JsonStreamReader jsr) {
    return parseEntry(null, ees, jsr);
  }

  private OEntity toOEntity(EdmEntitySet entitySet, EdmEntityType entityType, OEntityKey key, String entityTag, List<OProperty<?>> properties, List<OLink> links, Object... extensions) {

    // key is what we pulled out of the _metadata, use it first.
    if (key != null) {
      return OEntities.create(entitySet, entityType, key, entityTag, properties, links, extensions);
    }

    if (entityKey != null) {
      return OEntities.create(entitySet, entityType, entityKey, entityTag, properties, links, extensions);
    }

    return OEntities.createRequest(entitySet, properties, links);
  }

  protected JsonEntryMetaData parseMetadata(JsonStreamReader jsr) {
    JsonEntryMetaData jemd = new JsonEntryMetaData();
    ensureStartObject(jsr.nextEvent());

    while (jsr.hasNext()) {
      JsonEvent event = jsr.nextEvent();
      ensureNext(jsr);

      if (event.isStartProperty()
          && OdataJsonLiteConstant.TYPE_PROPERTY.equals(event.asStartProperty().getName())) {
        ensureEndProperty(event = jsr.nextEvent());
        jemd.type = event.asEndProperty().getValue();
      } else if (event.isStartProperty()
          && OdataJsonLiteConstant.ETAG_PROPERTY.equals(event.asStartProperty().getName())) {
        ensureEndProperty(event = jsr.nextEvent());
        jemd.etag = event.asEndProperty().getValue();
      } else if (event.isStartProperty()
          && OdataJsonLiteConstant.ACTIONS_PROPERTY.equals(event.asStartProperty().getName())) {
        parseFunctions(jsr, jemd.actions, FunctionKind.Action);
      } else if (event.isStartProperty()
          && OdataJsonLiteConstant.FUNCTIONS_PROPERTY.equals(event.asStartProperty().getName())) {
        parseFunctions(jsr, jemd.functions, FunctionKind.Function);
      } else if (event.isStartProperty() || event.isStartObject() || event.isStartArray()) {
        // ignore unsupported metadata, i.e. everything besides uri, type and etag
        jsr.skipNestedEvents();
      } else if (event.isEndObject()) {
        break;
      }
    }
    // eat the EndProperty event
    ensureEndProperty(jsr.nextEvent());

    return jemd;
  }

  protected void parseFunctions(JsonStreamReader jsr, Map<String, EdmFunctionImport> functions, FunctionKind kind) {
    JsonEvent event = null;
    ensureStartArray((event = jsr.nextEvent()));

    while (!(event = jsr.nextEvent()).isEndArray()) {
      String relation = jsr.nextEvent().asStartProperty().getName();
      ensureStartObject(event = jsr.nextEvent());

      String title = null;
      String target = null;

      while ((event = jsr.nextEvent()).isStartProperty()) {
        if (event.isStartProperty()
            && "title".equals(event.asStartProperty().getName())) {
          ensureEndProperty(event = jsr.nextEvent());
          title = event.asEndProperty().getValue();
        } else if (event.isStartProperty()
            && "target".equals(event.asStartProperty().getName())) {
          ensureEndProperty(event = jsr.nextEvent());
          target = event.asEndProperty().getValue();
        }

      }
      EdmEntitySet ees = metadata.getEdmEntitySet(entitySetName);
      EdmFunctionImport function = metadata.findEdmFunctionImport(title != null ? title : target, ees.getType(), kind);
      functions.put(relation, function);

      ensureEndProperty(event = jsr.nextEvent());
      ensureEndObject(event = jsr.nextEvent());
    }
    ensureEndProperty(event = jsr.nextEvent());
  }

  /**
   * adds the property. This property can be a navigation property too. In this
   * case a link will be added. If it's the meta data the information will be
   * added to the entry too.
   * @param rt 
   */
  protected EdmEntitySet addProperty(JsonEntry entry, EdmEntitySet ees, String name, JsonStreamReader jsr, JsonObjectPropertyValue rt) {

    if (OdataJsonLiteConstant.METADATA_PROPERTY.equals(name)) {
      JsonEntryMetaData jemd = parseMetadata(jsr);
      entry.jemd = jemd;
      JsonEvent event = jsr.nextEvent();
      ensureStartProperty(event);
      name = event.asStartProperty().getName();
      this.resolveEntityType(entry);
    }
    if (OdataJsonLiteConstant.ACTIONS_PROPERTY.equals(name)) {
      parseFunctions(jsr, entry.jemd.actions, FunctionKind.Action);
      name = jsr.nextEvent().asStartProperty().getName();
    }
    if (OdataJsonLiteConstant.FUNCTIONS_PROPERTY.equals(name)) {
      parseFunctions(jsr, entry.jemd.functions, FunctionKind.Function);
      name = jsr.nextEvent().asStartProperty().getName();
    }

    JsonEvent event = jsr.nextEvent();

    if (event.isEndProperty()) {
      // scalar property
      EdmProperty ep = entry.getEntityType().findProperty(name);
      if (ep == null) {
        if (event.asEndProperty().getValue() != null && name.contains(OdataJsonLiteConstant.NAV_LINK_URL_PROPERTY)) {
          String value = event.asEndProperty().getValue();
          String[] valueSplit = value.split("/");
          EdmNavigationProperty navProp = entry.getEntityType().findNavigationProperty(valueSplit[1]);
          if (navProp != null && navProp.getToRole().getMultiplicity() == EdmMultiplicity.MANY) {
            entry.links.add(OLinks.relatedEntities(valueSplit[1], valueSplit[1], rt.uri + "/" + valueSplit[1]));
          } else {
            entry.links.add(OLinks.relatedEntity(valueSplit[1], valueSplit[1], rt.uri + "/" + valueSplit[1]));
          }

          return ees;
        } else if (event.asEndProperty().getValue() == null) {
          removeExtraNavigationLink(name, entry);
          EdmNavigationProperty navProp = entry.getEntityType().findNavigationProperty(name);
          if (navProp != null) {
            entry.links.add(OLinks.relatedEntityInline(name, name, rt.uri + "/" + name, null));
            return ees;
          }
        }
        throw new IllegalArgumentException("unknown property " + name + " for " + entry.getEntityType().getName());
      }
      if (!ep.getType().isSimple()) {
        // the only context that lands us here is a null value for a complex property
        if (event.asEndProperty().getValue() == null) {
          EdmType propType = this.getPropertyType(ep);
          if (propType instanceof EdmCollectionType) {
            entry.properties.add(OProperties.collection(name, (EdmCollectionType) propType, null));
          } else {
            entry.properties.add(OProperties.complex(name, (EdmComplexType) propType, null));
          }
        } else {
          throw new UnsupportedOperationException("complex property unknown parse state");
        }
      } else {
        if (isResponse) {
          List<String> keys = entry.getEntityType().getKeys();
          String commaSeparator = ")";
          for (String keyName : keys) {
            if (ep.getName().equalsIgnoreCase(keyName)) {
              entityKeyCounter++;
              if (keys.size() > entityKeyCounter) {
                commaSeparator = ",";
              }
              entry.jemd.uri = entry.jemd.uri + keyName + "=" + getParsedValue(ep.getType(), event.asEndProperty().getValue()) + commaSeparator;
            }
          }
        }
        entry.properties.add(JsonTypeConverter.parse(name, (EdmSimpleType<?>) ep.getType(), event.asEndProperty().getValue(), event.asEndProperty().getValueTokenType()));
      }
    } else if (event.isStartObject()) {
      // reference deferred or inlined
      //handle complex object
      JsonObjectPropertyValue val = getValue(event, ees, name, jsr, entry);
      if (val.entity != null) {
        //remove the link which gets added to entry.links when we encounter odata.navigationLinkUrl.
        removeExtraNavigationLink(name, entry);
        entry.links.add(OLinks.relatedEntityInline(name, name, rt.uri + "/" + name,
            val.entity));
      }
      else if (val.complexObject != null) {
        entry.properties.add(OProperties.complex(name, (EdmComplexType) val.complexObject.getType(),
            val.complexObject.getProperties()));
      }
      else if (val.uri != null) {
        entry.links.add(OLinks.relatedEntity(name, name, val.uri));
      }
    } else if (event.isStartArray()) {
      ensureNext(jsr);
      JsonObjectPropertyValue val = getValue(event, ees, name, jsr, entry);
      if (val.entities != null) {
        //remove the link which gets added to entry.links when we encounter odata.navigationLinkUrl.
        removeExtraNavigationLink(name, entry);
        entry.links.add(OLinks.relatedEntitiesInline(name, name, rt.uri + "/" + name,
            val.entities));
      }
      else if (val.collection != null) {
        entry.properties.add(OProperties.collection(name, val.collectionType, val.collection));
      }

      else {
        List<OEntity> entities = new ArrayList<OEntity>();
        do {
          entities.add(parseEntry(ees, jsr).getEntity());
          event = jsr.nextEvent();
        } while (!event.isEndArray());
      }
    }
    return ees;
  }

  /**
   * Removes the extra navigation link.
   * 
   * @param name the name
   * @param entry the entry
   */
  private void removeExtraNavigationLink(String name, JsonEntry entry) {
    if (entry.links.size() - 1 >= 0) {
      OLink oLink = entry.links.get(entry.links.size() - 1);
      if (oLink.getTitle().equalsIgnoreCase(name)) {
        entry.links.remove(oLink);
      }
    }
  }

  private String getParsedValue(EdmType type, String value) {
    if (EdmSimpleType.STRING.equals(type) || EdmSimpleType.DATETIME.equals(type)) {
      return "'" + ConversionUtil.encodeString(value.replaceAll("'", "''")) + "'";
    }
    return value;
  }

  protected JsonObjectPropertyValue getValue(JsonEvent event, EdmEntitySet ees, String name, JsonStreamReader jsr, JsonEntry entry) {
    JsonObjectPropertyValue rt = new JsonObjectPropertyValue();

    EdmProperty eprop = entry.getEntityType().findProperty(name);
    EdmType propType = this.getPropertyType(eprop);
    if (propType instanceof EdmComplexType) {
      EdmComplexType complextype = this.metadata.findEdmComplexType(((EdmComplexType) propType).getFullyQualifiedTypeName());
      JsonLiteComplexObjectFormatParser cmp = new JsonLiteComplexObjectFormatParser(complextype);
      rt.complexObject = cmp.parseSingleObject(jsr, event);

    }
    else if (propType instanceof EdmCollectionType) {
      rt.collectionType = (EdmCollectionType) propType;
      JsonLiteCollectionFormatParser cfp = new JsonLiteCollectionFormatParser(rt.collectionType, this.metadata);
      rt.collection = cfp.parseCollection(jsr, event);

    }
    else if (event.isStartArray()) {
      List<OEntity> entities = new ArrayList<OEntity>();
      ensureNext(jsr);
      while ((jsr.nextEvent()).isStartObject()) {
        EdmNavigationProperty navProp = entry.getEntityType().findNavigationProperty(name);
        if (navProp != null) {
          ees = metadata.getEdmEntitySet(navProp.getToRole().getType());
          JsonEntry refEntry = parseEntry(null, ees, jsr);
          entities.add(toOEntity(ees, refEntry.getEntityType(), refEntry.getEntityKey(), refEntry.getETag(), refEntry.properties, refEntry.links));
        }
      }
      rt.entities = entities;
    }
    else if (event.isStartObject()) {
      EdmNavigationProperty navProp = entry.getEntityType().findNavigationProperty(name);
      if (navProp != null) {
        ees = metadata.getEdmEntitySet(navProp.getToRole().getType());
        if (jsr.nextEvent().asStartProperty().getName().equalsIgnoreCase("url")) {
          event = jsr.nextEvent();
          rt.uri = event.asEndProperty().getValue();
          //end object property
          ensureEndObject(jsr.nextEvent());
        } else {
          jsr.previousEvent();
          JsonEntry refEntry = parseEntry(null, ees, jsr);
          rt.entity = toOEntity(ees, refEntry.getEntityType(), refEntry.getEntityKey(), refEntry.getETag(), refEntry.properties, refEntry.links);
        }
      }
    }
    else {
      throw new RuntimeException("unhandled property: " + name);
    }

    return rt;
  }

  protected void ensureNext(JsonStreamReader jsr) {
    if (!jsr.hasNext()) {
      throw new IllegalArgumentException("no valid JSON format exepected at least one more event");
    }
  }

  protected void ensureStartProperty(JsonEvent event) {
    if (!event.isStartProperty()) {
      throw new IllegalArgumentException("no valid OData JSON format (expected StartProperty got " + event + ")");
    }
  }

  protected void ensureStartProperty(JsonEvent event, String name) {
    if (!(event.isStartProperty()
    && name.equals(event.asStartProperty().getName()))) {
      throw new IllegalArgumentException("no valid OData JSON format (expected StartProperty " + name + " got " + event + ")");
    }
  }

  protected void ensureEndProperty(JsonEvent event) {
    if (!event.isEndProperty()) {
      throw new IllegalArgumentException("no valid OData JSON format (expected EndProperty got " + event + ")");
    }
  }

  protected void ensureStartObject(JsonEvent event) {
    if (!event.isStartObject()) {
      throw new IllegalArgumentException("no valid OData JSON format expected StartObject got " + event + ")");
    }
  }

  protected void ensureEndObject(JsonEvent event) {
    if (!event.isEndObject()) {
      throw new IllegalArgumentException("no valid OData JSON format expected EndObject got " + event + ")");
    }
  }

  protected void ensureStartArray(JsonEvent event) {
    if (!event.isStartArray()) {
      throw new IllegalArgumentException("no valid OData JSON format expected StartArray got " + event + ")");
    }
  }

  protected void ensureEndArray(JsonEvent event) {
    if (!event.isEndArray()) {
      throw new IllegalArgumentException("no valid OData JSON format expected EndArray got " + event + ")");
    }
  }

  /**
   * this method will handle 2 different ways to specify collection type
   * 1. defined as <Property Name="Complexes" Nullable="true" Type="Collection<JsonTest.Complex1>" />
   * 2. defined as <Property CollectionKind="Collection" Name="Complexes" Nullable="true" Type="JsonTest.Complex1" />
   * @param eprop
   * @return
   */
  protected EdmType getPropertyType(EdmProperty eprop) {
    if (eprop == null) {
      return null;
    }
    EdmType propType = eprop.getType();
    // for odata4j generated collection property it is like 
    // <Property CollectionKind="Collection" Name="Complexes" Nullable="true" Type="JsonTest.Complex1" />
    if (eprop.getCollectionKind() != null && eprop.getCollectionKind() != CollectionKind.NONE) {
      propType = new EdmCollectionType(eprop.getCollectionKind(), propType);
    }

    return propType;

  }

}
