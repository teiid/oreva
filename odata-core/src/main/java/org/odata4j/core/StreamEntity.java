package org.odata4j.core;

public class StreamEntity implements OAtomStreamEntity {
  public String atomEntityType;
  public String atomEntitySource;
  
  @Override
  public String getAtomEntityType() {
    return atomEntityType;
  }

  @Override
  public String getAtomEntitySource() {
    return atomEntitySource;
  }

  /**
   * @param atomEntityType the atomEntityType to set
   */
  public void setAtomEntityType(String atomEntityType) {
    this.atomEntityType = atomEntityType;
  }

  /**
   * @param atomEntitySource the atomEntitySource to set
   */
  public void setAtomEntitySource(String atomEntitySource) {
    this.atomEntitySource = atomEntitySource;
  }
  
}
