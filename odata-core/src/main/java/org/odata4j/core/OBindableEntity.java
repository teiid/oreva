package org.odata4j.core;

import java.util.Map;

import org.odata4j.edm.EdmFunctionImport;

/**
 * An <code>OEntity</code> extension to link bindable functions or actions.
 *
 */
public interface OBindableEntity extends OExtension<OEntity> {
  
  /**
   * Returns a map of actions that can optionally be bound to the 
   * response entity.
   * 
   * @return a map of functions (fqActionName, function)
   */
  Map<String, EdmFunctionImport> getBindableActions();
  
  /**
   * Returns a map of functions that can optionally be bound to the 
   * response entity.
   * 
   * @return a map of functions (fqFunctionName, function)
   */  
  Map<String, EdmFunctionImport> getBindableFunctions();
  
}
