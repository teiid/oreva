package org.odata4j.core;

import org.odata4j.format.FormatType;

/**
 * Defined the interface that will be used in batch request. 
 * The interface must be implemented by request that can be part of the batch request like CRUD.
 *  
 * Copyright 2013 Halliburton
 * @author <a href="mailto:peng.chen@halliburton.com">Kevin Chen</a>
 *
 */
public interface OBatchSupport {
  /**
   * Format the locally built request to HTTP request string.
   * It will be used to form one part of the batch request sent to OData Service.
   * @param formatType either ATOM or JSON format
   * @return the request to be executed on OData Service.
   */
  String formatRequest(FormatType formatType);

  /**
   * Convert the HTTP response to object based on the request based on the request type.
   * For create entity/query single entity, the result is the single entity returned.
   * For query entities, the result is an Enumerable of entities as return from OQueryRequest
   * For delete/update, no object will be returned.
   * @param version indicate the ODataVersion returned.
   * @param payload the HTTP response payload from OData Service, the type can be String/Feed Reader/or MultiPart 
   *         the pay load will be converted to corresponding entities or other response type depending on the request. 
   * @param formatType either ATOM or JSON format
   * @return response result from the pay load. The result type depends on the request type
   *         for get/create entity request, the return type is OEntity
   *         for query entities/links request, the return type is Enumerable<OEntity>
   *         for entity count request, it is integer
   *         for change set request, it will not return anything.
   */
  Object getResult(ODataVersion version, Object payload, FormatType formatType);

}
