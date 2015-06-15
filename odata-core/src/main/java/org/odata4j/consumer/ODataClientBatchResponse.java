package org.odata4j.consumer;



/**
 * This interface define the result returned from batch request.
 * The result depends on request type.
 * All the request will return a staus code, but not all requests will return entity.
 * @author Kevin Chen
 *
 */
public interface ODataClientBatchResponse extends ODataClientResponse {
  /**
   * Return the status code of this individual request.
   * @return the status code.
   */
  public int getStatus();

  /**
   * Return the content. The content is based on the request. 
   * getEntities request ---> Enumerable<T>
   * getEntity request ------> T
   * createEntity request ----> T
   * update/delete request -----> null 
   * T is the entity type request.
   * @return the entities.
   */
  public Object getEntity();
}
