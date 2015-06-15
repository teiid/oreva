package org.odata4j.producer;

import org.odata4j.core.OEntity;
import org.odata4j.core.OExtension;
import org.odata4j.edm.EdmFunctionImport;

/**
 * An optional extension to query whether a function or an
 * action can be bound to an entity instance.
 * <p>To expose this extension, the producer implementation has to return an instance of this
 * interface when method {@code findExtension} is called and the first parameter is equal to
 * {@code OBindableFunctionExtension.class}.</p>
 */
public interface OBindableFunctionExtension extends OExtension<ODataProducer> {

  /**
   * Indicates if a function or an action can be bound with the current
   * entity instance
   * @param function  the function/action 
   * @param entity  the instance to check
   * @return true if the function can be bound for the given instance 
   */
  boolean isFunctionBindable(EdmFunctionImport function, OEntity entity);
}
