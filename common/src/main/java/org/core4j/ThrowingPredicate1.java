package org.core4j;

/**
 * Generic function implementation taking a single argument and returning true or false (or throwing a checked exception)
 * @param <T> Type of the predicate argument
 */
public interface ThrowingPredicate1<T> {

  /**
   * Apply this function, returning true or false (or throwing a checked exception)
   * @param input Predicate argument
   * @return true or false
   * @throws Exception A checked exception
   */
  boolean apply(T input) throws Exception;
}
