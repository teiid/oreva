package org.core4j.test;

import org.core4j.xml.XAttribute;
import org.core4j.xml.XDocument;
import org.core4j.xml.XElement;

public class TestXDocConstruction {

  public static void main(String[] args) {
    XDocument doc = new XDocument(new XElement("foo", new XAttribute("a", "1"), new XElement("bar")));
    System.out.println(doc);
  }
}
