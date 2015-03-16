package org.core4j.xml;

public class XComment extends XNode {

  private String value;

  public XComment(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public XmlNodeType getNodeType() {
    return XmlNodeType.COMMENT;
  }

  @Override
  public String toString() {
    return value;
  }

  @Override
  public String toString(XmlFormat format) {
    String indent = getIndent(format);
    return indent + "<!--" + value + "-->";
  }
}
