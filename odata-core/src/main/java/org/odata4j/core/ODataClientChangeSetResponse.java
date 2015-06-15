package org.odata4j.core;

import org.odata4j.consumer.ODataClientBatchResponse;

/**
 * The interface extends from ODataClientBatchResponse.
 * It add function to add one result of the change set operation.
 * 
 * Copyright 2013 Halliburton
 * @author <a href="mailto:peng.chen@halliburton.com">Kevin Chen</a>
 *
 */
public interface ODataClientChangeSetResponse extends ODataClientBatchResponse {
  /**
   * add one result into the change set result.
   * @param result, single operation result.
   */
  public void add(ODataClientBatchResponse result);

}
