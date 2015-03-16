package org.core4j;

/**
 * Generic function implementation taking a single argument and returning true or false
 * @param <T> Type of the predicate argument
 */
public interface Predicate1<T> {

  /**
   * Apply this function, returning true or false
   * @param input Predicate argument
   * @return true or false
   */
  boolean apply(T input);
}
