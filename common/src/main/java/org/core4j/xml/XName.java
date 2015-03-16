package org.core4j.xml;

public class XName {

  private final XNamespace namespace;
  private final String localName;

  public XName(XNamespace namespace, String localName) {
    this.namespace = namespace;
    this.localName = localName;
  }

  public String getNamespaceName() {
    return getNamespace().getNamespaceName();
  }

  public XNamespace getNamespace() {
    return namespace;
  }

  public String getLocalName() {
    return localName;
  }

  @Override
  public String toString() {
    return namespace == null ? "" : ("{" + namespace + "}") + localName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((localName == null) ? 0 : localName.hashCode());
    result = prime * result
        + ((namespace == null) ? 0 : namespace.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    XName other = (XName) obj;
    if (localName == null) {
      if (other.localName != null) {
        return false;
      }
    } else if (!localName.equals(other.localName)) {
      return false;
    }
    if (namespace == null) {
      if (other.namespace != null) {
        return false;
      }
    } else if (!namespace.equals(other.namespace)) {
      return false;
    }
    return true;
  }
}
