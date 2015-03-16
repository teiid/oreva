package org.core4j;

/**
 * Generic function implementation taking two arguments and returning a value
 * @param <T1> Type of the first argument
 * @param <T2> Type of the second argument
 * @param <TResult> Type of the return value
 */
public interface Func2<T1, T2, TResult> {

  /**
   * Apply this function, returning the result
   * @param input1 First function argument
   * @param input2 Second function argument
   * @return Function result
   */
  TResult apply(T1 input1, T2 input2);
}
