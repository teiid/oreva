package org.core4j.xml;

public class XNamespace {

  private final String namespaceName;

  public XNamespace(String namespaceName) {
    this.namespaceName = namespaceName;
  }

  public String getNamespaceName() {
    return namespaceName;
  }

  @Override
  public String toString() {
    return namespaceName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((namespaceName == null) ? 0 : namespaceName.hashCode());
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
    XNamespace other = (XNamespace) obj;
    if (namespaceName == null) {
      if (other.namespaceName != null) {
        return false;
      }
    } else if (!namespaceName.equals(other.namespaceName)) {
      return false;
    }
    return true;
  }
}
