package org.core4j;

import java.lang.reflect.Field;

import org.core4j.xml.XName;
import org.core4j.xml.XNameable;

public class Predicates {

  public static Predicate1<String> endsWith(final String suffix) {
    return new Predicate1<String>() {
      public boolean apply(String input) {
        return input.endsWith(suffix);
      }
    };
  }

  public static Predicate1<String> startsWith(final String prefix) {
    return new Predicate1<String>() {
      public boolean apply(String input) {
        return input.startsWith(prefix);
      }
    };
  }

  public static <T extends XNameable> Predicate1<T> xnameEquals(final XName xname) {
    return new Predicate1<T>() {
      public boolean apply(T input) {
        return input.getName().equals(xname);
      }
    };
  }

  public static <T extends XNameable> Predicate1<T> xnameEquals(final String name) {
    final XName xname = new XName(null, name);
    return new Predicate1<T>() {
      public boolean apply(T input) {
        return input.getName().equals(xname);
      }
    };
  }

  public static <T, TField> Predicate1<T> byField(Class<T> clazz, String fieldName, Class<TField> fieldClass, final TField fieldValue) {
    final Field field = CoreUtils.getField(clazz, fieldName);
    field.setAccessible(true);
    return wrap(new ThrowingPredicate1<T>() {
      @SuppressWarnings("unchecked")
      public boolean apply(T input) throws Exception {
        TField value = (TField) field.get(input);
        if (value == null) {
          return fieldValue == null;
        } else {
          return value.equals(fieldValue);
        }
      }
    });
  }

  public static <T> Predicate1<T> not(final Predicate1<T> predicate) {
    return new Predicate1<T>() {
      public boolean apply(T input) {
        return !predicate.apply(input);
      }
    };
  }

  public static <T> Predicate1<T> wrap(final ThrowingPredicate1<T> fn) {
    return new Predicate1<T>() {
      public boolean apply(T input) {
        try {
          return fn.apply(input);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    };
  }
}
