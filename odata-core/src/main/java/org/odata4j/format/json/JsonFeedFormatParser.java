package org.odata4j.format.json;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader.JsonEvent;
import org.odata4j.urlencoder.ConversionUtil;

public class JsonFeedFormatParser extends JsonFormatParser implements FormatParser<Feed> {

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

    private static final Pattern ENTITY_SET_NAME = Pattern.compile("\\/([^\\/\\(]+)\\(");

    public OEntityKey getEntityKey() {
      String uri = getUri();
      if (uri == null)
        return null;

      Matcher m = ENTITY_SET_NAME.matcher(uri);

      // Fix for NPE when  
      // 1. nested entity like /Categories(1)/Products(76) is requested and
      // 2. entity with keys like /PointSetField(attribute='X (EASTING)',point_set_id=19)

      int count = 0;
      int index = 0;
      while (m.find()) {
        count++;
        index = m.end();
      }
      if (count == 0)
        throw new RuntimeException("Unable to parse the entity-key from atom entry id: " + uri);

      //key(s) is the last occurrence in the pattern match
      return OEntityKey.parse(ConversionUtil.decodeString(uri.substring(index - 1)));
      //   return OEntityKey.parse(uri.substring(uri.lastIndexOf('(')));
    }

  }

  public JsonFeedFormatParser(Settings settings) {
    super(settings);
  }

  @Override
  public JsonFeed parse(Reader reader) {
    JsonStreamReader jsr = JsonStreamReaderFactory.createJsonStreamReader(reader);
    boolean hasResultsProp = false;
    boolean isSingleEntity = (super.entityKey != null);
    try {
      // {
      JsonEvent event = null;
      ensureStartObject(jsr.nextEvent());

      // "d" :
      ensureStartProperty(jsr.nextEvent(), DATA_PROPERTY);

      if (!isSingleEntity && version.compareTo(ODataVersion.V1) > 0) {
        // {
        ensureStartObject(jsr.nextEvent());
        // results only for collections, if it is single entity or property it won't be there
        // "results" :
        event = jsr.nextEvent();
        // if it is start property, check if its results/__metada and then skip them
        if (event.isStartProperty()) {
          if (event.asStartProperty().getName().equals((RESULTS_PROPERTY))) {
            hasResultsProp = true;
            // skip [
            event = jsr.nextEvent();
          } else if (event.asStartProperty().getName().equals(METADATA_PROPERTY)) {
            // this is __metadata, skip it
            event = jsr.nextEvent();
            ensureStartObject(event);
            int soCount = 1; // count the start object and end object to know when the __metadata ends
            while (soCount > 0) {
              event = jsr.nextEvent();
              if (event.isStartObject()) {
                soCount++;
              } else if (event.isEndObject()) {
                soCount--;
              }
            }
          }
        }
      } else {
        // skip {
        event = jsr.nextEvent();
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

      event = jsr.nextEvent();

      while (event.isStartProperty()) {
        String pname = event.asStartProperty().getName();
        ensureNext(jsr);
        ensureEndProperty(event = jsr.nextEvent());
        if (NEXT_PROPERTY.equals(pname)) {
          feed.next = event.asEndProperty().getValue();
        } else if (COUNT_PROPERTY.equals(pname)) {
          feed.inlineCount = Integer.parseInt(event.asEndProperty().getValue());
        }
        ensureNext(jsr);
        event = jsr.nextEvent();
      }

      if (hasResultsProp) {
        // EndObject and EndProperty of "result" :
        ensureEndObject(event);
        ensureEndProperty(jsr.nextEvent());
      }

      ensureEndObject(jsr.nextEvent());

      if (jsr.hasNext())
        throw new IllegalArgumentException("garbage after the feed");

      return feed;

    } finally {
      jsr.close();
    }
  }

}
