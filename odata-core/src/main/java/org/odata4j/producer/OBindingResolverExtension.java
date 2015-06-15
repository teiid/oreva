package org.odata4j.producer;

import org.odata4j.core.OEntityKey;
import org.odata4j.core.OExtension;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.edm.EdmFunctionImport;

/**
 * An extension to provide a way to resolve entity from references
 * used as bound function or action parameters.
 * <p>To expose this extension, the producer implementation has to return an instance of this
 * interface when method {@code findExtension} is called and the first parameter is equal to
 * {@code OBindingResolverExtension.class}.</p>
 */
public interface OBindingResolverExtension extends OExtension<ODataProducer> {

  /**
   * Resolves the binding parameter of a function or action
   * based on the given query information and the entity set and key.
   * The resulting parameter will be used as binding parameter
   * during the call of the callFunction method of the ODataProducer.
   * 
   * @param context  Context of the request
   * @param function  EdmFunctionImport called
   * @param entitySetName  the entity set to query
   * @param entityKey  the entity key, can be null if referring a collection 
   * @param queryInfo  the search parameters
   * @return an OFunctionParameter fully initialized
   */
  OFunctionParameter resolveBindingParameter(
      ODataContext context, 
      EdmFunctionImport function, 
      String entitySetName,
      OEntityKey entityKey,
      QueryInfo queryInfo);
  
}
