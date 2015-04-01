package org.odata4j.core;

import org.odata4j.exceptions.ODataProducerException;

/**
 * Interface for update a named stream request.
 * @author <a href="mailto:kevin.chen@halliburton.com">Kevin Chen</a>
 *
 */
public interface OUpdateNamedStreamRequest {

  void execute() throws ODataProducerException;

}
