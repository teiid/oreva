package org.core4j.xml;

public class XText extends XNode {

  private String value;

  public XText(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public XmlNodeType getNodeType() {
    return XmlNodeType.TEXT;
  }

  @Override
  public String toString() {
    return toString(XmlFormat.NOT_INDENTED);
  }

  @Override
  public String toString(XmlFormat format) {
    String escaped = XmlUtil.escapeElementValue(value);
    return (format.isIndentEnabled() ? escaped.trim() : escaped);
  }
}
