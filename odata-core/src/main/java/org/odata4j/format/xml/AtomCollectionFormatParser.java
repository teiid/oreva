package org.odata4j.format.xml;

import java.io.Reader;

import org.odata4j.core.OCollection;
import org.odata4j.core.OCollections;
import org.odata4j.core.OComplexObject;
import org.odata4j.core.OSimpleObject;
import org.odata4j.core.OSimpleObjects;
import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmType;
import org.odata4j.format.FormatParser;
import org.odata4j.format.Settings;
import org.odata4j.stax2.Attribute2;
import org.odata4j.stax2.QName2;
import org.odata4j.stax2.StartElement2;
import org.odata4j.stax2.XMLEvent2;
import org.odata4j.stax2.XMLEventReader2;
import org.odata4j.stax2.util.StaxUtil;

/**
 * 
 * Copyright 2013 Halliburton
 * @author <a href="mailto:peng.chen@halliburton.com">Kevin Chen</a>
 *
 */
public class AtomCollectionFormatParser implements FormatParser<OCollection<?>> {
  private final Settings settings;
  
  /**
   * constructor.
   * @param setting the parseType should be the itemType of the collection
   */
  public AtomCollectionFormatParser (Settings setting) {
    EdmType itemType = ((EdmCollectionType)setting.parseType).getItemType();
    this.settings = new Settings(setting.version, setting.metadata, setting.entitySetName, setting.entityKey, setting.fcMapping, setting.isResponse, itemType, setting.parseFunction);
  }
  
  /**
   * Constructor.
   * @param itemType the collection's item type.
   * @param md the metadat.
   * @param isResponse is this a response. not used for now.
   */
  public AtomCollectionFormatParser(EdmType itemType, EdmDataServices md, boolean isResponse) {
    this.settings = new Settings(null, md, null, null, null, isResponse, itemType);
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public OCollection<?> parse(Reader reader) {
    XMLEventReader2 xmlReader = StaxUtil.newXMLEventReader(reader);
    xmlReader.nextEvent(); // start document
    XMLEvent2 startElementEvent = xmlReader.nextEvent(); // start element

    return AtomCollectionFormatParser.parse(xmlReader, startElementEvent.asStartElement(), this.settings.metadata, this.settings.parseType);
   }

  public static OCollection parse(XMLEventReader2 reader, StartElement2 startElement, EdmDataServices metadata, EdmType itemType) {
    QName2 seName = startElement.getName();
    Attribute2 seTypeAttribute = startElement.getAttributeByName(XmlFormatParser.M_TYPE);
    Attribute2 seNullAttribute = startElement.getAttributeByName(XmlFormatParser.M_NULL);
    boolean seIsNull = seNullAttribute != null && "true".equals(seNullAttribute.getValue());

    if (seIsNull) {
      return null;
    }
  
    OCollection.Builder rt = OCollections.newBuilder(itemType);
  
    while (reader.hasNext()) {
    XMLEvent2 event = reader.nextEvent();
    
    // end of the element, return
    if (event.isEndElement() && event.asEndElement().getName().equals(seName)) {
      return rt.build();
    }
    
    if (event.isStartElement() && event.asStartElement().getName().getNamespaceUri().equals(XmlFormatParser.NS_DATASERVICES)) {
//      String name = event.asStartElement().getName().getLocalPart();
//      Attribute2 typeAttribute = event.asStartElement().getAttributeByName(XmlFormatParser.M_TYPE);
      Attribute2 nullAttribute = event.asStartElement().getAttributeByName(XmlFormatParser.M_NULL);
      boolean isNull = nullAttribute != null && "true".equals(nullAttribute.getValue());

      // for simple types
      if (itemType.isSimple()) {
        OSimpleObject<?> simple = OSimpleObjects.parse((EdmSimpleType) itemType, isNull? null: reader.getElementText());
        rt.add(simple);
      } else if (itemType instanceof EdmComplexType){
        OComplexObject complex = AtomComplexFormatParser.parse(reader, event.asStartElement(), metadata, (EdmComplexType) itemType);
        rt.add(complex);
      } else if (itemType instanceof EdmCollectionType) {
        OCollection col = AtomCollectionFormatParser.parse(reader, startElement, metadata, ((EdmCollectionType)itemType).getItemType());
        rt.add(col);
      }
    }
  }
   
  throw new RuntimeException("cannot find the matching end element");
  }

}
