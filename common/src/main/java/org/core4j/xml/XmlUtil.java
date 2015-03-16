package org.core4j.xml;

public class XmlUtil {

  public static String escapeAttributeValue(String unescaped) {
    return escapeElementValue(unescaped); // TODO for now
  }

  public static String escapeElementValue(String unescaped) {
    return unescaped.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "&apos;").replace("\"", "&quot;");
  }
}
