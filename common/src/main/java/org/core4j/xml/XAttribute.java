package org.core4j.xml;

public class XAttribute extends XObject implements XNameable {

  private XName name;
  private String value;

  public XAttribute(String name, Object value) {
    this.name = new XName(null, name);
    this.value = value.toString();
  }

  public String getValue() {
    return value;
  }

  public XName getName() {
    return name;
  }

  @Override
  public XmlNodeType getNodeType() {
    return XmlNodeType.ATTRIBUTE;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
