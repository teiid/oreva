package org.odata4j.format.jsonlite;

import java.io.Reader;

import org.odata4j.core.OError;
import org.odata4j.format.FormatParser;
import org.odata4j.format.Settings;

/**
 * The Class JsonLiteErrorFormatParser.
 * 
 *@author <a href="mailto:shantanu@synerzip.com">Shantanu Dindokar</a>
 */
public class JsonLiteErrorFormatParser extends JsonLiteFormatParser implements FormatParser<OError> {

  public JsonLiteErrorFormatParser(Settings settings) {
    super(settings);
  }

  @Override
  public OError parse(Reader reader) {
    //TODO : need to provide implementation for ERROR 
    return null;
  }
}
