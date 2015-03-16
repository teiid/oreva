package org.core4j;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class ReadOnlyIterator<T> implements Iterator<T> {

  public static class IterationResult<T> {

    public final boolean hasNext;
    public final T current;

    private IterationResult(boolean hasNext, T current) {
      this.hasNext = hasNext;
      this.current = current;
    }

    public static <T> IterationResult<T> done() {
      return new IterationResult<T>(false, null);
    }

    public static <T> IterationResult<T> next(T value) {
      return new IterationResult<T>(true, value);
    }
  }

  enum HasNext {

    UNKNOWN,
    YES,
    NO;

    public boolean getBoolean() {
      if (this == UNKNOWN) {
        throw new IllegalArgumentException();
      }
      return this == YES;
    }
  }

  private HasNext hasNext = HasNext.UNKNOWN;
  private T current;

  public boolean hasNext() {
    if (hasNext == HasNext.UNKNOWN) {
      IterationResult<T> result = null;
      try {
        result = this.advance();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      if (result == null) {
        result = IterationResult.done();
      }
      hasNext = result.hasNext ? HasNext.YES : HasNext.NO;
      current = result.current;
    }
    return hasNext.getBoolean();
  }

  public T next() {
    if (this.hasNext()) {
      hasNext = HasNext.UNKNOWN;
      return current;
    } else {
      throw new NoSuchElementException();
    }
  }

  public void remove() {
    throw new UnsupportedOperationException("remove()");
  }

  protected abstract IterationResult<T> advance() throws Exception;
}
