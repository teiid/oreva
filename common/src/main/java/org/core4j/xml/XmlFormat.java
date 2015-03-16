package org.core4j.xml;

public class XmlFormat {

  public static final XmlFormat NOT_INDENTED = new XmlFormat(false, " ", 0);
  public static final XmlFormat INDENTED = new XmlFormat(true, "  ", 0);
  private final boolean indentEnabled;
  private final String indentString;
  private final int currentIndent;

  private XmlFormat(boolean indentEnabled, String indentString, int currentIndent) {
    this.indentEnabled = indentEnabled;
    this.indentString = indentString;
    this.currentIndent = currentIndent;
  }

  public int getCurrentIndent() {
    return currentIndent;
  }

  public String getIndentString() {
    return indentString;
  }

  public boolean isIndentEnabled() {
    return indentEnabled;
  }

  public XmlFormat incrementLevel() {
    return new XmlFormat(indentEnabled, indentString, currentIndent + 1);
  }
}
