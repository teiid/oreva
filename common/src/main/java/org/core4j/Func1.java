package org.core4j;

/**
 * Generic function implementation taking a single argument and returning a value
 * @param <T> Type of the function argument
 * @param <TResult> Type of the return value
 */
public interface Func1<T, TResult> {

  /**
   * Apply this function, returning the result
   * @param input Function argument
   * @return Function result
   */
  TResult apply(T input);
}
