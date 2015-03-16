package org.core4j.xml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class XDocument extends XContainer {

  private XElement documentElement;

  public XDocument(XElement documentElement) {
    this.documentElement = documentElement;
    this.add(documentElement);
  }

  private XDocument(Document document) {
    for (Node childNode : domNodes(document.getChildNodes())) {
      XNode xchild = parseNode(childNode);
      if (xchild instanceof XElement) {
        this.documentElement = (XElement) xchild;
      }
      add(xchild);
    }
    //this.documentElement = parse(document.getDocumentElement());
  }

  @Override
  public String toString() {
    return toString(XmlFormat.NOT_INDENTED);
  }

  @Override
  public String toString(XmlFormat format) {
    StringBuilder sb = new StringBuilder();
    for (XNode node : nodes()) {
      if (node instanceof XProcessingInstruction || node instanceof XElement || node instanceof XComment || node instanceof XDocumentType) {
        sb.append(node.toString(format));
      } else {
        throw new UnsupportedOperationException("implement " + node);
      }
    }
    return sb.toString();
  }

  @Override
  protected XElement getXElement() {
    return documentElement;
  }

  public XElement getRoot() {
    return documentElement;
  }

  @Override
  public XmlNodeType getNodeType() {
    return XmlNodeType.DOCUMENT;
  }

  //	@Override
  //	public Enumerable<XNode> nodes() {
  //		//return Enumerable.create(documentElement).cast(XNode.class);
  //		return 
  //	}
  public static XDocument parse(String text) {
    return load(new StringReader(text));
  }

  public static XDocument loadUtf8(InputStream inputStream) {
    try {
      return load(new InputStreamReader(inputStream, "UTF8"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static XDocument load(Reader reader) {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(new InputSource(reader));
      return new XDocument(document);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
