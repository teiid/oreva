package org.core4j.xml;

public class XProcessingInstruction extends XNode {

  private final String target;
  private final String data;

  public XProcessingInstruction(String target, String data) {
    this.target = target;
    this.data = data;
  }

  public String getTarget() {
    return target;
  }

  public String getData() {
    return data;
  }

  @Override
  public XmlNodeType getNodeType() {
    return XmlNodeType.PROCESSING_INSTRUCTION;
  }

  @Override
  public String toString(XmlFormat format) {
    String indent = getIndent(format);
    return indent + "<?" + target + " " + data + ">";
  }
}
