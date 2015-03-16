package org.core4j;

/**
 * Generic function implementation taking no arguments and returning a value
 * @param <TResult> Type of the return value
 */
public interface Func<TResult> {

  /** 
   * Apply this function, returning the result
   * @return Function result
   */
  TResult apply();
}
