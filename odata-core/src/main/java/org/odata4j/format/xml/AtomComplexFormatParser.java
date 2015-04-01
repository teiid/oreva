package org.odata4j.format.xml;

import java.io.Reader;
import java.util.Iterator;
import org.odata4j.core.OComplexObject;
import org.odata4j.core.OComplexObject.Builder;
import org.odata4j.core.OComplexObjects;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.FormatParser;
import org.odata4j.format.Settings;
import org.odata4j.stax2.Attribute2;
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
public class AtomComplexFormatParser implements FormatParser<OComplexObject>{

  Settings settings;
  
  public AtomComplexFormatParser(EdmDataServices md, EdmComplexType complexType) {
    settings = new Settings(null, md, null, null, null, true, complexType);
  }
  
  public AtomComplexFormatParser(Settings settings) {
    this.settings = settings;  
  }

  /**
   * Parse a complex type from function/action response.
   */
  @Override
  public OComplexObject parse(Reader reader) {
    XMLEvent2 event;
    XMLEventReader2 xmlReader = StaxUtil.newXMLEventReader(reader);
    
    xmlReader.nextEvent(); // start document;
    event = xmlReader.nextEvent(); // start element
    
    String name = event.asStartElement().getName().getLocalPart();
    Attribute2 typeAttribute = event.asStartElement().getAttributeByName(XmlFormatParser.M_TYPE);
    Attribute2 nullAttribute = event.asStartElement().getAttributeByName(XmlFormatParser.M_NULL);
    boolean isNull = nullAttribute != null && "true".equals(nullAttribute.getValue());
    
    if (isNull) {
      return null;
    }

    System.out.println("the complex type name is:"+name);

    
    Iterable<OProperty<?>> props = AtomFeedFormatParser.parseProperties(xmlReader, event.asStartElement(), settings.metadata, (EdmComplexType) settings.parseType);
    
    return createCT(props, (EdmComplexType) this.settings.parseType);
  }
  
  /**
   * this parse an complex object within another element 
   */
  public static OComplexObject parse(XMLEventReader2 reader, StartElement2 propertiesElement, EdmDataServices metadata, EdmComplexType ctType) {
    Attribute2 nullAttribute = propertiesElement.getAttributeByName(XmlFormatParser.M_NULL);
    boolean isNull = nullAttribute != null && "true".equals(nullAttribute.getValue());
    
    if (isNull) {
      return null;
    }

    Iterable<OProperty<?>> props = AtomFeedFormatParser.parseProperties(reader, propertiesElement, metadata, ctType);

    return createCT(props, ctType);
  }
  
  private static OComplexObject createCT(Iterable<OProperty<?>> props, EdmComplexType type) {
    Builder builder = OComplexObjects.newBuilder(type);
    Iterator<OProperty<?>> it = props.iterator();
    while (it.hasNext()) {
      OProperty<?> o = it.next();
      builder.add(o);
    }
    return builder.build();

  }
  
}
