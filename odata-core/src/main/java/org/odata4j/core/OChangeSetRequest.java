package org.odata4j.core;

import java.util.List;

/**
 * The consumer side change set request builder, used in batch request.
 * The change set can contain create/update/delete single entity operation 
 * and they will be one single transaction boundary. It cannot contain query 
 * operation or another change set.
 * All the operations within a change set request will be defined as single
 * transaction unit. 
 * 
 * Copyright 2013 Halliburton
 * @author <a href="mailto:peng.chen@halliburton.com">Kevin Chen</a>
 */
public interface OChangeSetRequest extends OBatchSupport {
  /**
   * Add a single create request.
   * @param request the request to be part of the change set request.
   * @return the change set request builder.
   */
  public OChangeSetRequest addRequest(OCreateRequest<?> request);

  /**
   * Add a single update request.
   * @param request the request to be part of the change set request.
   * @return the change set request builder.
   */
  public OChangeSetRequest addRequest(OModifyRequest<?> request);

  /**
   * Add a single delete request.
   * @param request the request to be part of the change set request.
   * @return the change set request builder.
   */
  public OChangeSetRequest addRequest(OEntityDeleteRequest request);
  
  /**
   * Add a single create/update/delete link request.
   * @param request the request to be part of the change set request.
   * @return the change set request builder.
   */
  public OChangeSetRequest addRequest(OModifyLinkRequest request);

  /**
   * Return a list of request within the change set.
   * @return the list of the request.
   */
  public List<OBatchSupport> getReqs();

}
