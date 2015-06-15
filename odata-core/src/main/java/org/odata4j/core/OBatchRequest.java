package org.odata4j.core;

import java.util.List;

import org.odata4j.consumer.ODataClientBatchResponse;
import org.odata4j.exceptions.ODataProducerException;

/**
 * Consumer side batch request builder. 
 * The batch request can contain a series of query request and ChangeSet request. 
 * The ChangeSet can contain a list of Create/Update/Delete request.
 * All requests that will be participated in the batch operation must implement {@link OBatchSupport} interface.
 * 
 * Copyright 2013 Halliburton
 * @author <a href="mailto:peng.chen@halliburton.com">Kevin Chen</a>
 *
 */
public interface OBatchRequest {
  /**
   * Add a query single entity request to batch.
   * @param request the query request to be added.
   * @return the batch request builder.
   */
  public OBatchRequest addRequest(OEntityGetRequest<?> request);

  /**
   * Add a entities query request to batch.
   * @param request the entities queries request to be added.
   * @return the batch request builder.
   */
  public OBatchRequest addRequest(OQueryRequest<?> request);

  /**
   * Add a count request to the batch.
   * @param countRequest the count request to be added
   * @return the batch request builder.
   */
  public OBatchRequest addRequest(OCountRequest countRequest);

  /**
   * Add a change set request to the batch.
   * @param changeSetRequest to be added.
   * @return the batch request builder
   */
  public OBatchRequest addRequest(OChangeSetRequest changeSetRequest);

  /**
   * Send the batch request to OData Service and return a list of the response.
   * The order of the response list matches the request list.
   * @return a list of results for the batch request. 
   * @throws ODataProducerException  error from the producer
   */
  public List<ODataClientBatchResponse> execute() throws ODataProducerException;
}
