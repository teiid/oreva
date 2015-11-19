package org.odata4j.core;

/**
 *  OEntity extension to support streaming entities.
 */
public interface OAtomStreamEntity extends OExtension<OEntity> {

  /**
   * Gets the stream content-type.
   * @return String
   */
  String getAtomEntityType();

  /**
   * Gets the stream src uri.
   * @return String
   */
  String getAtomEntitySource();

}