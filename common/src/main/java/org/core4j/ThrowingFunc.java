package org.core4j;

/**
 * Generic function implementation taking no arguments and returning a value (or throwing a checked exception)
 * @param <TResult> Type of the return value
 */
public interface ThrowingFunc<TResult> {

  /**
   * Apply this function, returning the result (or throwing a checked exception)
   * @return Function result
   * @throws Exception A checked exception
   */
  TResult apply() throws Exception;
}
