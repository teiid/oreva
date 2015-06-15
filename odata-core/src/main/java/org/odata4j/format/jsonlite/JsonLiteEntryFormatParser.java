package org.odata4j.format.jsonlite;

import java.io.Reader;

import org.odata4j.format.Entry;
import org.odata4j.format.FormatParser;
import org.odata4j.format.Settings;
import org.odata4j.format.json.JsonStreamReaderFactory;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader;

/**
 * The Class JsonLiteEntryFormatParser.
 * 
 * @author <a href="mailto:shantanu@synerzip.com">Shantanu Dindokar</a>
 */
public class JsonLiteEntryFormatParser extends JsonLiteFormatParser implements FormatParser<Entry> {

  public JsonLiteEntryFormatParser(Settings settings) {
    super(settings);
  }

  @Override
  public Entry parse(Reader reader) {
    JsonStreamReader jsr = JsonStreamReaderFactory.createJsonStreamReader(reader);
    try {
      ensureNext(jsr);

      // skip the StartObject event
      ensureStartObject(jsr.nextEvent());

      if (isResponse) {
        ensureNext(jsr);
      }

      // parse the entry
      return parseEntry(metadata.getEdmEntitySet(entitySetName), jsr);

      // no interest in the closing events
    } finally {
      jsr.close();
    }
  }

}
