package org.odata4j.core;

/**
 * An annotation (typed name-value pair) that lives in a namespace.
 */
public interface NamespacedAnnotation<T> extends NamedValue<T> {

  PrefixedNamespace getNamespace();

  Class<T> getValueType();

}
