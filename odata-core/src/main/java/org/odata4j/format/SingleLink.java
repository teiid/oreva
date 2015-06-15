package org.odata4j.format;

import org.odata4j.core.OEntityId;

public interface SingleLink {

  String getUri();

  String getTargetNavProp();

  OEntityId getSourceEntity();
}
