package org.core4j.xml;

public class XDocumentType extends XNode {

  private final String name;
  private final String internalSubset;

  public XDocumentType(String name, String internalSubset) {
    this.name = name;
    this.internalSubset = internalSubset;
  }

  @Override
  public XmlNodeType getNodeType() {
    return XmlNodeType.DOCUMENT_TYPE;
  }

  @Override
  public String toString(XmlFormat format) {
    String indent = getIndent(format);
    return indent + "<!DOCTYPE " + name + " [\n" + internalSubset + "]>\n";
  }
}
