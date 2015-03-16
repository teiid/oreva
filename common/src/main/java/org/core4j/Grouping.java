package org.core4j;

public class Grouping<TKey, TElement> extends Enumerable<TElement> {

  private final TKey key;

  public Grouping(TKey key, Enumerable<TElement> values) {
    super(values);
    this.key = key;
  }

  public TKey getKey() {
    return key;
  }
}
