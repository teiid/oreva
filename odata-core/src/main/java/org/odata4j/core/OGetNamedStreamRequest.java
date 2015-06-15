package org.odata4j.core;

import java.io.InputStream;

import org.odata4j.exceptions.ODataProducerException;

/**
 * A request to get named stream request.
 * @author <a href="mailto:kevin.chen@halliburton.com">Kevin Chen</a>
 *
 */
public interface OGetNamedStreamRequest {
  /**
   * get the input stream for the named resource stream.
   * @return the input stream.
   * @throws ODataProducerException
   */
  InputStream execute() throws ODataProducerException;
}
