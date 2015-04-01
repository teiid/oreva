package org.odata4j.core;

/**
 * Marker interface for createLink/updateLink/deleteLink request.
 * Separate it from OEntityRequest. It will be used in Change Set Request, 
 * so that query request cannot be added into the change set.
 * 
 * Copyright 2013 Halliburton
 * @author <a href="mailto:peng.chen@halliburton.com">Kevin Chen</a>
 *
 */
public interface OModifyLinkRequest extends OEntityRequest<Void> {

}
