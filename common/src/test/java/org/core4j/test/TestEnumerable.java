package org.core4j.test;

import java.util.Iterator;

import junit.framework.Assert;

import org.core4j.Enumerable;
import org.core4j.Enumerables;
import org.core4j.Func;
import org.core4j.Func1;
import org.core4j.Funcs;
import org.core4j.Predicate1;
import org.core4j.Predicates;
import org.core4j.ReadOnlyIterator;
import org.junit.Test;

public class TestEnumerable {

  @Test
  public void testEnumerable() {

    Assert.assertEquals(5, Enumerable.range(1, 5).count());
    Assert.assertEquals((Integer) 1, Enumerable.range(1, 5).first());
    Assert.assertEquals((Integer) 5, Enumerable.range(1, 5).last());
    Assert.assertEquals((Integer) 3, Enumerable.range(1, 5).elementAt(2));
    Assert.assertEquals(null, Enumerable.empty(Integer.class).firstOrNull());
    Assert.assertEquals("1", Enumerable.create(1).join(","));

    Assert.assertEquals("1,2,3,4,5", Enumerable.range(1, 5).join(","));
    Assert.assertEquals("5,4,3,2,1", Enumerable.range(1, 5).reverse().join(","));
    Assert.assertEquals("10", Enumerable.range(10, 1).join(","));
    Assert.assertEquals("1", Enumerable.range(1, 1000000).take(1).join(","));
    Assert.assertEquals("3,4,5", Enumerable.range(1, 5).skip(2).join(","));
    Assert.assertEquals("2,3,4,5", Enumerable.range(1, 5).skipWhile(IS_ODD).join(","));
    Assert.assertEquals((Integer) 10, Enumerable.range(1, 4).sum(Integer.class));

    Enumerable<Integer> one = Enumerable.create(5, 3, 9, 7, 5, 9, 3, 7);
    Enumerable<Integer> two = Enumerable.create(8, 3, 6, 4, 4, 9, 1, 0);

    Assert.assertEquals((Integer) 0, two.min(IDENTITY));
    Assert.assertEquals((Integer) 9, two.max(IDENTITY));
    Assert.assertEquals("5,3,9,7", one.distinct().join(","));
    Assert.assertEquals("5,3,9,7,8,6,4,1,0", one.union(two).join(","));
    Assert.assertEquals("5,3,9,7,5,9,3,7,8,3,6,4,4,9,1,0", one.concat(two).join(","));
    Assert.assertEquals("3,9", one.intersect(two).join(","));

    Assert.assertEquals("3,9,1", two.where(IS_ODD).join(","));
    Assert.assertEquals("8,6,4,4,0", two.where(Predicates.not(IS_ODD)).join(","));
    Assert.assertEquals("2,4,6,8,10", Enumerable.range(1, 5).select(TIMES_TWO).join(","));

    Assert.assertEquals("onetwothree", Enumerable.create("one", "two", "three").selectMany(CHARS).join(""));

    // test using an infinite iterator - none of these methods should materialize the enumerable
    Assert.assertEquals("1,1", infinite(1).skip(100).take(2).join(","));
    Assert.assertEquals(true, infinite(1).any(IS_ODD));
    Assert.assertEquals(true, infinite(1).contains(1));
    Assert.assertEquals((Integer) 1, infinite(1).first());
    Assert.assertEquals((Integer) 1, infinite(1).elementAt(100));
    Assert.assertEquals((Integer) 2, infinite(1).select(TIMES_TWO).first());
    Assert.assertEquals((Integer) 1, infinite(1).where(IS_ODD).first());
    Assert.assertEquals((Integer) 1, infinite(1).cast(Integer.class).first());
    Assert.assertEquals("oneone", infinite("one").selectMany(CHARS).take(6).join(""));
    Assert.assertEquals("1,1", infinite(1).concat(infinite(1)).take(2).join(","));
  }

  private static <T> Enumerable<T> infinite(final T value) {
    return Enumerable.createFromIterator(new Func<Iterator<T>>() {
      public Iterator<T> apply() {
        return new InfiniteIterator<T>(value);
      }
    });
  }

  private static class InfiniteIterator<T> extends ReadOnlyIterator<T> {

    private final T value;

    public InfiniteIterator(T value) {
      this.value = value;
    }

    @Override
    protected IterationResult<T> advance() throws Exception {
      return IterationResult.next(value);
    }
  }

  private static final Func1<Integer, Integer> IDENTITY = Funcs.identity(Integer.class);
  
  private static final Func1<Integer, Integer> TIMES_TWO = new Func1<Integer, Integer>() {
    public Integer apply(Integer input) {
      return input * 2;
    }
  };
  
  private static final Predicate1<Integer> IS_ODD = new Predicate1<Integer>() {
    public boolean apply(Integer input) {
      return input % 2 == 1;
    }
  };
  
  private static final Func1<String, Enumerable<Character>> CHARS = new Func1<String, Enumerable<Character>>() {
    public Enumerable<Character> apply(String input) {
      return Enumerables.chars(input);
    }
  };
}
