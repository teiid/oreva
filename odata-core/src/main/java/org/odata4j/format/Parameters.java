package org.odata4j.format;

import java.util.Collection;

import org.odata4j.core.OFunctionParameter;

/**
 * Building block for OData function parameters payload.
 *
 */
public interface Parameters {

  public Collection<OFunctionParameter> getParameters();
  
}
