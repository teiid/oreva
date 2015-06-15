package org.odata4j.format.jsonlite;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.odata4j.core.OCollection;
import org.odata4j.core.OComplexObject;
import org.odata4j.core.ODataVersion;
import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmProperty.CollectionKind;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.format.FormatParser;
import org.odata4j.format.Settings;
import org.odata4j.format.SingleLink;
import org.odata4j.format.SingleLinks;
import org.odata4j.format.json.JsonStreamReaderFactory;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader.JsonEvent;

/**
 * The Class JsonLiteSingleLinkFormatParser.
 * 
 * @author <a href="mailto:shantanu@synerzip.com">Shantanu Dindokar</a>
 */
public class JsonLiteSingleLinkFormatParser extends JsonLiteFormatParser implements FormatParser<SingleLink> {
   
  public JsonLiteSingleLinkFormatParser(Settings settings) {
    super(settings);
  }

  @Override
  public SingleLink parse(Reader reader) {
    // {"url": "http://host/service.svc/Orders(1)"}
    JsonStreamReader jsr = JsonStreamReaderFactory.createJsonStreamReader(reader);
    try {
      while (jsr.hasNext()) {
        JsonEvent event = jsr.nextEvent();
        if (event.isStartProperty() && event.asStartProperty().getName().equals("url")) {
          String uri = jsr.nextEvent().asEndProperty().getValue();
          return SingleLinks.create(uri);
        }
      }
      throw new RuntimeException("JsonLiteSingleLinkFormatParser: failed to get the link from json string.");
    } finally {
      jsr.close();
    }
  }
  
  /**
   * Parses the links.
   *
   * @param reader the reader
   * @return the iterable 
   */
  @SuppressWarnings("unchecked")
  public static Iterable<SingleLink> parseLinks(Reader reader) {
    EdmProperty.Builder ctPropBuilder = EdmProperty.newBuilder("url").setType(EdmSimpleType.STRING);
    List<EdmProperty.Builder> complexTypeBuilder = new ArrayList<EdmProperty.Builder>();
    complexTypeBuilder.add(ctPropBuilder);

    EdmComplexType.Builder ctBuilder = new EdmComplexType.Builder().addProperties(complexTypeBuilder).setName("urlLinks");
    EdmComplexType uriCtType = ctBuilder.build();
    Settings s = new Settings(ODataVersion.V3, null, null, null, null, true, new EdmCollectionType(CollectionKind.Collection, uriCtType));
    JsonLiteCollectionFormatParser jsonLiteCollectionFormatParser = new JsonLiteCollectionFormatParser(s);
    OCollection<OComplexObject> linkCollection = (OCollection<OComplexObject>)jsonLiteCollectionFormatParser.parse(reader);

    List<SingleLink> rt = new ArrayList<SingleLink>();
    for (OComplexObject obj : linkCollection) {
      rt.add(SingleLinks.create(obj.getProperty("url", String.class).getValue()));
    }

    return rt;
  }
}
