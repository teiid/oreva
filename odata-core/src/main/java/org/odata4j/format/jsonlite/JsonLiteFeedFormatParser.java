package org.odata4j.format.jsonlite;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.odata4j.core.ODataConstants;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OLink;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.format.Entry;
import org.odata4j.format.Feed;
import org.odata4j.format.FormatParser;
import org.odata4j.format.Settings;
import org.odata4j.format.json.JsonStreamReaderFactory;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader.JsonEvent;
import org.odata4j.urlencoder.ConversionUtil;

/**
 * The Class JsonLiteFeedFormatParser. Testing done for simple type of entities as we
 * proceed with the implementation we will test the remaining scenarios with different type of
 * entities.
 * 
 * @author <a href="mailto:shantanu@synerzip.com">Shantanu Dindokar</a>
 */
public class JsonLiteFeedFormatParser extends JsonLiteFormatParser implements FormatParser<Feed> {

  private String metadataType;

  public JsonLiteFeedFormatParser(Settings settings) {
    super(settings);
  }

  public JsonLiteFeedFormatParser(Settings settings, String metadataType) {
    super(settings);
    this.metadataType = metadataType;
  }

  static class JsonFeed implements Feed {
    List<Entry> entries;
    String next;
    Integer inlineCount;

    @Override
    public String getNext() {
      return next;
    }

    @Override
    public Iterable<Entry> getEntries() {
      return entries;
    }

  }

  static class JsonEntry implements Entry {

    private EdmEntitySet entitySet;
    private EdmEntityType entityType;

    JsonEntryMetaData jemd;
    List<OProperty<?>> properties;
    List<OLink> links;
    OEntity oentity;

    public JsonEntry(EdmEntitySet eset, JsonEntryMetaData jemd) {
      this.entitySet = eset;
      this.entityType = eset != null ? eset.getType() : null;
      this.jemd = jemd;
    }

    public String getContentType() {
      return ODataConstants.APPLICATION_JAVASCRIPT_CHARSET_UTF8;
    }

    public JsonEntryMetaData getJemd() {
      return this.jemd;
    }

    public EdmEntitySet getEntitySet() {
      return this.entitySet;
    }

    public EdmEntityType getEntityType() {
      return this.entityType;
    }

    public void setEntityType(EdmEntityType value) {
      this.entityType = value;
    }

    @Override
    public String getUri() {
      return jemd == null ? null : jemd.uri;
    }

    public String getETag() {
      return jemd == null ? null : jemd.etag;
    }

    @Override
    public OEntity getEntity() {
      return oentity;
    }

    public OEntityKey getEntityKey() {
      String uri = getUri();
      if (uri == null)
        return null;
      if (uri.equalsIgnoreCase(OdataJsonLiteConstant.START_URI))
        return null;
      //key(s) is the last occurrence in the pattern match
      return OEntityKey.parse(ConversionUtil.decodeString(uri));
      //   return OEntityKey.parse(uri.substring(uri.lastIndexOf('(')));
    }

  }

  @Override
  public JsonFeed parse(Reader reader) {
    JsonStreamReader jsr = JsonStreamReaderFactory.createJsonStreamReader(reader);
    boolean hasResultsProp = false;
    boolean isSingleEntity = (super.entityKey != null);
    try {
      // {
      JsonEvent event = null;
      event = jsr.nextEvent();
      ensureStartObject(event);

      if (!isSingleEntity && version.compareTo(ODataVersion.V1) > 0) {
        // results only for collections, if it is single entity or property it won't be there
        // "results" :
        event = jsr.nextEvent();
        // if it is start property, check if its results/__metada and then skip them
        if (event.isStartProperty()) {
          if (event.asStartProperty().getName().equals(OdataJsonLiteConstant.METADATA_PROPERTY)) {
            // this is odata.metadata, skip it
            event = jsr.nextEvent();
            ensureEndProperty(event);
            event = jsr.nextEvent();
            if (event.asStartProperty().getName().equals((OdataJsonLiteConstant.VALUE_PROPERTY))) {
              hasResultsProp = true;
              // skip [
              event = jsr.nextEvent();
            }
          }
          if (event.isStartProperty() && event.asStartProperty().getName().equals((OdataJsonLiteConstant.VALUE_PROPERTY))) {
            hasResultsProp = true;
            // skip [
            event = jsr.nextEvent();
          }
        }
      } else {
        if (metadataType != null && !metadataType.equalsIgnoreCase(OdataJsonLiteConstant.METADATA_TYPE_NOMETADATA)) {
          event = jsr.nextEvent();
        }
      }

      JsonFeed feed;
      if (event.isStartArray()) {
        feed = parseFeed(metadata.getEdmEntitySet(entitySetName), jsr);
        // ] already processed by parseFeed
      } else {
        feed = new JsonFeed();
        feed.entries = new ArrayList<Entry>();
        feed.entries.add(parseEntry(metadata.getEdmEntitySet(entitySetName), jsr));
        // } already processed by parseEntry
      }

      if (hasResultsProp) {
        // EndProperty of "results" :
        ensureEndProperty(jsr.nextEvent());
      }
      if (!jsr.hasNext())
        return feed;

      event = jsr.nextEvent();

      while (event.isStartProperty()) {
        String pname = event.asStartProperty().getName();
        ensureNext(jsr);
        ensureEndProperty(event = jsr.nextEvent());
        if (OdataJsonLiteConstant.NEXT_PROPERTY.equals(pname)) {
          feed.next = event.asEndProperty().getValue();
        } else if (OdataJsonLiteConstant.COUNT_PROPERTY.equals(pname)) {
          feed.inlineCount = Integer.parseInt(event.asEndProperty().getValue());
        }
        ensureNext(jsr);
        event = jsr.nextEvent();
      }

      ensureEndObject(event);

      if (jsr.hasNext())
        throw new IllegalArgumentException("garbage after the feed");

      return feed;

    } finally {
      jsr.close();
    }
  }
}
