package org.core4j;

/**
 * Generic function implementation taking a single argument and returning a value (or throwing a checked exception)
 * @param <T> Type of the function argument
 * @param <TResult> Type of the return value
 */
public interface ThrowingFunc1<T, TResult> {

  /**
   * Apply this function, returning the result (or throwing a checked exception)
   * @param input Function argument
   * @return Function result
   * @throws Exception A checked exception
   */
  TResult apply(T input) throws Exception;
}
