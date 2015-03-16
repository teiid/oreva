package org.core4j;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Enumerable<T> implements Iterable<T> {

  private final Iterable<T> values;

  protected Enumerable(Iterable<T> values) {
    if (values == null) {
      throw new RuntimeException("values cannot be null");
    }
    this.values = values;
  }

  public static <T> Enumerable<T> create(T... values) {
    return new Enumerable<T>(new ArrayIterable<T>(values));
  }

  public static <T> Enumerable<T> create(Iterable<T> values) {
    return new Enumerable<T>(values);
  }

  @SuppressWarnings("unchecked")
  public static <T> Enumerable<T> create(Class<T> clazz, Enumeration<?> e) {
    List<T> rt = new ArrayList<T>();
    while (e.hasMoreElements()) {
      rt.add((T) e.nextElement());
    }
    return new Enumerable<T>(rt);
  }

  public static <T> Enumerable<T> createFromIterator(final Func<Iterator<T>> fn) {
    return new Enumerable<T>(makeIterable(fn));
  }

  @SuppressWarnings("unchecked")
  public T[] toArray(Class<T> clazz) {
    List<T> rt = toList();
    T[] array = (T[]) Array.newInstance(clazz, rt.size());
    for (int i = 0; i < array.length; i++) {
      array[i] = rt.get(i);
    }
    return array;

  }

  public List<T> toList() {
    List<T> rt = new ArrayList<T>();
    for (T value : values) {
      rt.add(value);
    }
    return rt;
  }

  public Set<T> toSet() {
    Set<T> rt = new HashSet<T>();
    for (T value : values) {
      rt.add(value);
    }
    return rt;
  }

  public SortedSet<T> toSortedSet() {
    SortedSet<T> rt = new TreeSet<T>();
    for (T value : values) {
      rt.add(value);
    }
    return rt;
  }

  public SortedSet<T> toSortedSet(Comparator<? super T> comparator) {
    SortedSet<T> rt = new TreeSet<T>(comparator);
    for (T value : values) {
      rt.add(value);
    }
    return rt;
  }

  public <K> Map<K, T> toMap(Func1<T, K> keyFn) {
    Map<K, T> rt = new HashMap<K, T>();
    for (T value : values) {
      rt.put(keyFn.apply(value), value);
    }
    return rt;
  }

  public int count() {
    int rt = 0;
    for (@SuppressWarnings("unused")
    T value : values) {
      rt++;
    }
    return rt;
  }

  public T first() {
    for (T value : values) {
      return value;
    }
    throw new RuntimeException("No elements");
  }

  public T first(Predicate1<T> predicate) {
    for (T value : values) {
      if (predicate.apply(value)) {
        return value;
      }
    }
    throw new RuntimeException("No elements match the predicate");
  }

  public T firstOrNull() {
    for (T value : values) {
      return value;
    }
    return null;
  }

  public T firstOrNull(Predicate1<T> predicate) {
    for (T value : values) {
      if (predicate.apply(value)) {
        return value;
      }
    }
    return null;
  }

  public Enumerable<T> where(Predicate1<T> predicate) {
    return new Enumerable<T>(new PredicateIterable<T>(this, predicate));
  }

  public <TOutput> Enumerable<TOutput> select(Func1<T, TOutput> projection) {
    return new Enumerable<TOutput>(new FuncIterable<T, TOutput>(this, projection));
  }

  public static <K, E> Map<K, List<E>> group(Collection<E> c, final Func1<E, K> projection) {
    Map<K, List<E>> map = new HashMap<K, List<E>>();
    for (E e : c) {
      K key = projection.apply(e);
      if (key != null) {
        List<E> list = map.get(key);
        if (list == null) {
          list = new ArrayList<E>();
          map.put(key, list);
        }
        list.add(e);
      }
    }
    return map;
  }

  public T last() {
    T rt = null;
    boolean empty = true;
    for (T value : values) {
      empty = false;
      rt = value;
    }
    if (empty) {
      throw new RuntimeException("No elements");
    }
    return rt;
  }

  public Iterator<T> iterator() {
    return values.iterator();
  }

  public Enumerable<T> reverse() {
    List<T> rt = this.toList();
    Collections.reverse(rt);
    return new Enumerable<T>(rt);
  }

  private List<Iterable<T>> thisThenOthers(Iterable<T>... others) {
    List<Iterable<T>> rt = new ArrayList<Iterable<T>>();
    rt.add(this);
    for (Iterable<T> other : others) {
      rt.add(other);
    }
    return rt;
  }

  @SuppressWarnings("unchecked")
  public Enumerable<T> concat(Iterable<T> other) {
    return concat(new Iterable[] { other });
  }

  public Enumerable<T> concat(Iterable<T>... others) {
    List<Iterable<T>> rt = thisThenOthers(others);
    return new Enumerable<T>(new ConcatIterable<T>(rt));
  }

  @SuppressWarnings("unchecked")
  public Enumerable<T> concat(T... others) {
    return concat(new Enumerable[] { Enumerable.create(others) });
  }

  public Enumerable<T> take(final int count) {
    return createFromIterator(new Func<Iterator<T>>() {
      public Iterator<T> apply() {
        return new TakeIterator<T>(Enumerable.this, count);
      }
    });
  }

  private static class TakeIterator<T> extends ReadOnlyIterator<T> {

    private int left;
    private Iterator<T> iterator;

    public TakeIterator(Iterable<T> values, int count) {
      iterator = values.iterator();
      left = count;
    }

    @Override
    protected IterationResult<T> advance() throws Exception {
      if (left <= 0) {
        return IterationResult.done();
      }

      if (!iterator.hasNext()) {
        return IterationResult.done();
      }

      left--;

      return IterationResult.next(iterator.next());
    }
  }

  public boolean any(Predicate1<T> predicate) {
    for (T value : values) {
      if (predicate.apply(value)) {
        return true;
      }
    }
    return false;
  }

  public boolean all(Predicate1<T> predicate) {
    for (T value : values) {
      if (!predicate.apply(value)) {
        return false;
      }
    }
    return true;
  }

  public boolean contains(T value) {
    for (T existingValue : values) {
      if (existingValue.equals(value)) {
        return true;
      }
    }
    return false;

  }

  public T elementAt(int index) {
    int i = 0;
    for (T value : values) {
      if (index == i++) {
        return value;
      }
    }
    throw new RuntimeException("No element at index " + index);
  }

  public T elementAtOrNull(int index) {
    int i = 0;
    for (T value : values) {
      if (index == i++) {
        return value;
      }
    }
    return null;
  }

  public <TReturn> TReturn aggregate(Class<TReturn> clazz, Func2<T, TReturn, TReturn> aggregation) {
    return aggregate(clazz, null, aggregation);
  }

  public <TReturn> TReturn aggregate(Class<TReturn> clazz, TReturn initialValue, Func2<T, TReturn, TReturn> aggregation) {
    TReturn rt = initialValue;
    for (T value : values) {
      rt = aggregation.apply(value, rt);
    }
    return rt;
  }

  public <TReturn> TReturn sum(final Class<TReturn> clazz) {
    if (clazz.equals(Double.class) || clazz.equals(Integer.class) || clazz.equals(BigDecimal.class)) {
      Func2<T, TReturn, TReturn> aggregation = new Func2<T, TReturn, TReturn>() {
        @SuppressWarnings("unchecked")
        public TReturn apply(T input1, TReturn input2) {

          Number n1 = (Number) input1; // assumes T (this) is Number
          Number n2 = (Number) input2; // this is safe, one of Double,Integer,BigDecimal

          // TODO better way?
          if (clazz.equals(Double.class)) {
            Double rt = n1.doubleValue() + (n2 == null ? 0 : n2.doubleValue());
            return (TReturn) rt;
          }
          if (clazz.equals(Integer.class)) {
            Integer rt = n1.intValue() + (n2 == null ? 0 : n2.intValue());
            return (TReturn) rt;
          }
          if (clazz.equals(BigDecimal.class)) {
            if (n1 instanceof Integer) {
              n1 = BigDecimal.valueOf((Integer) n1);
            }
            if (n1 instanceof Double) {
              n1 = BigDecimal.valueOf((Double) n1);
            }
            BigDecimal bd1 = n1 == null ? BigDecimal.ZERO : (BigDecimal) n1;
            BigDecimal bd2 = n2 == null ? BigDecimal.ZERO : (BigDecimal) n2;

            BigDecimal rt = bd1.add(bd2);
            return (TReturn) rt;
          }

          throw new UnsupportedOperationException("No default aggregation for class " + clazz.getSimpleName());
        }
      };
      return aggregate(clazz, aggregation);
    }

    throw new UnsupportedOperationException("No default aggregation for class " + clazz.getSimpleName());
  }

  public <TReturn> TReturn sum(Class<TReturn> clazz, Func1<T, TReturn> projection) {
    Enumerable<TReturn> rt = this.select(projection);
    return rt.sum(clazz);
  }

  private static class ArrayIterable<T> implements Iterable<T> {

    private final T[] values;

    public ArrayIterable(T[] values) {
      this.values = values;
    }

    public Iterator<T> iterator() {
      return new ArrayIterator<T>(values);
    }
  }

  private static class ArrayIterator<T> implements Iterator<T> {

    private final T[] values;
    private int current = -1;

    public ArrayIterator(T[] values) {
      this.values = values;
    }

    public boolean hasNext() {
      return current < (values.length - 1);
    }

    public T next() {
      try {
        return values[++current];
      } catch (ArrayIndexOutOfBoundsException e) {
        throw new NoSuchElementException();
      }
    }

    public void remove() {
      throw new UnsupportedOperationException("remove()");
    }
  }

  private static class PredicateIterable<T> implements Iterable<T> {

    private final Iterable<T> iterable;
    private final Predicate1<T> predicate;

    public PredicateIterable(Iterable<T> i, Predicate1<T> p) {
      this.iterable = i;
      this.predicate = p;
    }

    public Iterator<T> iterator() {
      return new PredicateIterator<T>(iterable.iterator(), predicate);
    }
  }

  private static class PredicateIterator<T> extends ReadOnlyIterator<T> {

    private final Iterator<T> iterator;
    private final Predicate1<T> predicate;
    private boolean useEx = true; // ( slightly faster in perf tests)

    public PredicateIterator(Iterator<T> i, Predicate1<T> p) {
      this.iterator = i;
      this.predicate = p;
    }

    @Override
    protected IterationResult<T> advance() {
      // exception-backed method 
      if (useEx) {
        try {
          T rt = iterator.next();
          while (!predicate.apply(rt)) {
            rt = iterator.next();
          }
          return IterationResult.next(rt);
        } catch (NoSuchElementException e) {
          return IterationResult.done();
        }
      } else {
        // non-exception-backed method
        if (iterator.hasNext()) {
          T rt = iterator.next();
          while (!predicate.apply(rt)) {
            if (iterator.hasNext()) {
              rt = iterator.next();
            } else {
              return IterationResult.done();
            }
          }
          return IterationResult.next(rt);
        } else {
          return IterationResult.done();
        }
      }
    }
  }

  private static class FuncIterable<X, Y> implements Iterable<Y> {

    private final Iterable<X> iterable;
    private final Func1<X, Y> projection;

    public FuncIterable(Iterable<X> iterable, Func1<X, Y> projection) {
      this.iterable = iterable;
      this.projection = projection;
    }

    public Iterator<Y> iterator() {
      return new FuncIterator<X, Y>(iterable.iterator(), projection);
    }
  }

  private static class FuncIterator<X, Y> implements Iterator<Y> {

    private final Iterator<X> iterator;
    private final Func1<X, Y> projection;

    public FuncIterator(Iterator<X> iterator, Func1<X, Y> projection) {
      this.iterator = iterator;
      this.projection = projection;
    }

    public boolean hasNext() {
      return iterator.hasNext();
    }

    public Y next() {
      return projection.apply(iterator.next());
    }

    public void remove() {
      iterator.remove();
    }
  }

  private static class ConcatIterable<T> implements Iterable<T> {

    private final Iterable<Iterable<T>> iterables;

    public ConcatIterable(Iterable<Iterable<T>> iterables) {
      this.iterables = iterables;
    }

    public Iterator<T> iterator() {
      return new ConcatIterator<T>(Enumerable.create(iterables).select(new Func1<Iterable<T>, Iterator<T>>() {
        public Iterator<T> apply(Iterable<T> x) {
          return x.iterator();
        }
      }).toList());
    }
  }

  private static class ConcatIterator<T> implements Iterator<T> {

    private final List<Iterator<T>> iterators;
    private int current;

    public ConcatIterator(List<Iterator<T>> iterators) {
      this.iterators = iterators;
    }

    public boolean hasNext() {
      boolean rt = iterators.get(current).hasNext();
      while (!rt) {
        if (current == iterators.size() - 1) {
          return rt;
        }
        current++;
        rt = iterators.get(current).hasNext();
      }
      return rt;
    }

    public T next() {
      while (true) {
        try {
          return iterators.get(current).next();
        } catch (NoSuchElementException e) {
          if (current == iterators.size() - 1) {
            throw new NoSuchElementException();
          }
          current++;
        }
      }
    }

    public void remove() {
      throw new UnsupportedOperationException("remove()");
    }
  }

  public <TKey extends Comparable<TKey>> Enumerable<T> orderBy(final Func1<T, TKey> projection) {
    return orderBy(new Comparator<T>() {
      public int compare(T o1, T o2) {
        TKey lhs = projection.apply(o1);
        TKey rhs = projection.apply(o2);
        return lhs.compareTo(rhs);
      }
    });
  }

  public Enumerable<T> orderBy(Comparator<T> comparator) {
    List<T> rt = this.toList();
    Collections.sort(rt, comparator);
    return Enumerable.create(rt);
  }

  public Enumerable<T> orderBy() {
    return orderBy(new Comparator<T>() {
      @SuppressWarnings("unchecked")
      public int compare(T o1, T o2) {
        Comparable<T> lhs = (Comparable<T>) o1;
        return lhs.compareTo(o2);
      }
    });
  }

  public String join(String separator) {
    StringBuilder rt = new StringBuilder();
    boolean isFirst = true;
    for (T value : this) {
      if (isFirst) {
        isFirst = false;
      } else {
        rt.append(separator);
      }
      rt.append(value == null ? "" : value.toString());
    }
    return rt.toString();
  }

  @SuppressWarnings("unchecked")
  public static <T> Enumerable<T> empty(Class<T> clazz) {
    return Enumerable.create();
  }

  public static Enumerable<Integer> range(final int start, final int count) {
    return createFromIterator(new Func<Iterator<Integer>>() {
      public Iterator<Integer> apply() {
        return new RangeIterator(start, count);
      }
    });
  }

  private static class RangeIterator extends ReadOnlyIterator<Integer> {

    private final int end;
    private Integer current;

    public RangeIterator(int start, int count) {
      current = start;
      end = start + count - 1;
    }

    @Override
    protected IterationResult<Integer> advance() throws Exception {
      if (current == null) {
        return IterationResult.done();
      }
      int rt = current;
      if (rt == end) {
        current = null;
      } else {
        current = rt + 1;
      }
      return IterationResult.next(rt);
    }
  }

  public <TOutput> Enumerable<TOutput> cast(Class<TOutput> clazz) {
    return this.select(new Func1<T, TOutput>() {
      @SuppressWarnings("unchecked")
      public TOutput apply(T input) {
        return (TOutput) input;
      }
    });
  }

  public <TOutput> Enumerable<TOutput> ofType(Class<TOutput> clazz) {
    final Class<TOutput> finalClazz = clazz;
    return this.where(new Predicate1<T>() {
      public boolean apply(T input) {
        return input != null && finalClazz.isAssignableFrom(input.getClass());
      }
    }).cast(clazz);
  }

  public Enumerable<T> skip(int count) {
    return Enumerable.create(new SkipEnumerable<T>(this, count));
  }

  private static class SkipEnumerable<T> implements Iterable<T> {

    private final Enumerable<T> target;
    private final int count;

    public SkipEnumerable(Enumerable<T> target, int count) {
      this.target = target;
      this.count = count;
    }

    public Iterator<T> iterator() {
      Iterator<T> rt = target.iterator();
      for (int i = 0; i < count; i++) {
        if (!rt.hasNext()) {
          return rt;
        }
        rt.next();
      }
      return rt;
    }
  }

  public Enumerable<T> skipWhile(final Predicate1<T> predicate) {
    final Boolean[] skipping = new Boolean[] { true };
    return this.where(new Predicate1<T>() {
      public boolean apply(T input) {
        if (!skipping[0]) {
          return true;
        }
        if (!predicate.apply(input)) {
          skipping[0] = false;
          return true;
        }
        return false;
      }
    });
  }

  @SuppressWarnings("unchecked")
  public Enumerable<T> intersect(Enumerable<T> other) {
    return intersect(new Enumerable[] { other });
  }

  public Enumerable<T> intersect(Enumerable<T>... others) {
    List<T> rt = this.distinct().toList();
    for (Enumerable<T> other : others) {
      Set<T> set = other.toSet();
      for (T value : Enumerable.create(rt).toList()) {
        if (!set.contains(value)) {
          rt.remove(value);
        }
      }
    }
    return Enumerable.create(rt);
  }

  @SuppressWarnings("unchecked")
  public Enumerable<T> union(Enumerable<T> other) {
    return union(new Enumerable[] { other });
  }

  public Enumerable<T> union(Enumerable<T>... others) {
    final List<Iterable<T>> rt = thisThenOthers(others);
    return Enumerable.create(makeIterable(new Func<Iterator<T>>() {
      public Iterator<T> apply() {
        return new UnionIterator<T>(rt);
      }
    }));
  }

  private static class UnionIterator<T> extends ReadOnlyIterator<T> {

    private final List<Iterable<T>> involved;

    public UnionIterator(List<Iterable<T>> involved) {
      this.involved = involved;
    }

    private Set<T> seen;
    private int currentIndex = -1;
    private Iterator<T> currentIterator;

    @Override
    protected IterationResult<T> advance() {
      if (seen == null) {
        seen = new HashSet<T>();
      }
      while (true) {
        if (currentIterator == null) {
          currentIndex++;
          if (currentIndex >= involved.size()) {
            return IterationResult.done();
          }
          currentIterator = involved.get(currentIndex).iterator();
        }
        if (!currentIterator.hasNext()) {
          currentIterator = null;
        } else {
          T value = currentIterator.next();
          if (!seen.contains(value)) {
            seen.add(value);
            return IterationResult.next(value);
          }
        }
      }
    }
  }

  private static <T> Iterable<T> makeIterable(final Func<Iterator<T>> fn) {
    return new Iterable<T>() {
      public Iterator<T> iterator() {
        return fn.apply();
      }
    };
  }

  public <TResult> Enumerable<TResult> selectMany(final Func1<T, Enumerable<TResult>> selector) {
    return Enumerable.createFromIterator(new Func<Iterator<TResult>>() {
      public Iterator<TResult> apply() {
        return new SelectManyIterator<T, TResult>(Enumerable.this, selector);
      }
    });
  }

  private static class SelectManyIterator<TSource, TResult> extends ReadOnlyIterator<TResult> {

    private final Iterator<TSource> sourceIterator;
    private final Func1<TSource, Enumerable<TResult>> selector;
    private Iterator<TResult> resultIterator;

    public SelectManyIterator(Iterable<TSource> source, Func1<TSource, Enumerable<TResult>> selector) {
      this.selector = selector;
      this.sourceIterator = source.iterator();
    }

    @Override
    protected IterationResult<TResult> advance() throws Exception {
      while (true) {
        if (resultIterator == null) {
          if (!sourceIterator.hasNext()) {
            return IterationResult.done();
          }
          TSource source = sourceIterator.next();
          resultIterator = selector.apply(source).iterator();
        }
        if (!resultIterator.hasNext()) {
          resultIterator = null;
        } else {
          return IterationResult.next(resultIterator.next());
        }
      }
    }
  }

  public Enumerable<T> distinct() {
    return Enumerable.createFromIterator(new Func<Iterator<T>>() {
      public Iterator<T> apply() {
        return new DistinctIterator<T>(Enumerable.this);
      }
    });
  }

  private static class DistinctIterator<T> extends ReadOnlyIterator<T> {

    private final Iterator<T> iterator;
    private Set<T> seen;

    public DistinctIterator(Iterable<T> source) {
      iterator = source.iterator();
    }

    @Override
    protected IterationResult<T> advance() throws Exception {
      if (seen == null) {
        seen = new HashSet<T>();
      }
      while (iterator.hasNext()) {
        T value = iterator.next();
        if (!seen.contains(value)) {
          seen.add(value);
          return IterationResult.next(value);
        }
      }
      return IterationResult.done();
    }
  }

  public <TKey> Enumerable<Grouping<TKey, T>> groupBy(Func1<T, TKey> keySelector) {
    List<TKey> ordering = new ArrayList<TKey>();
    final Map<TKey, List<T>> map = new HashMap<TKey, List<T>>();
    for (T value : this) {
      TKey key = keySelector.apply(value);
      if (!ordering.contains(key)) {
        ordering.add(key);
        map.put(key, new ArrayList<T>());
      }
      map.get(key).add(value);
    }

    return Enumerable.create(ordering).select(new Func1<TKey, Grouping<TKey, T>>() {
      public Grouping<TKey, T> apply(TKey input) {
        return new Grouping<TKey, T>(input, Enumerable.create(map.get(input)));
      }
    });
  }

  public <TResult extends Comparable<TResult>> TResult max(Func1<T, TResult> fn) {
    TResult rt = null;
    for (T value : this) {
      TResult newValue = fn.apply(value);
      if (newValue == null) {
        continue;
      }
      if (rt == null) {
        rt = newValue;
      } else {
        if (newValue.compareTo(rt) > 0) {
          rt = newValue;
        }
      }
    }
    return rt;
  }

  public <TResult extends Comparable<TResult>> TResult min(Func1<T, TResult> fn) {
    TResult rt = null;
    for (T value : this) {
      TResult newValue = fn.apply(value);
      if (newValue == null) {
        continue;
      }
      if (rt == null) {
        rt = newValue;
      } else {
        if (newValue.compareTo(rt) < 0) {
          rt = newValue;
        }
      }
    }
    return rt;
  }
}
