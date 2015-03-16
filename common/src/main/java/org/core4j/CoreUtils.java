package org.core4j;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoreUtils {

  public static String replace(String input, String regex, int options, Func1<Matcher, String> evaluator) {
    Pattern pattern = Pattern.compile(regex, options);
    Matcher matcher = pattern.matcher(input);
    StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      String replacement = evaluator.apply(matcher);
      matcher.appendReplacement(sb, replacement);
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  public static Field getField(Class<?> type, String name) {
    while (!type.equals(Object.class)) {
      for (Field f : type.getDeclaredFields()) {
        if (f.getName().equals(name)) {
          return f;
        }
      }
      type = type.getSuperclass();
    }
    throw new RuntimeException("Field not found: " + name);
  }

  @SuppressWarnings("unchecked")
  public static <T> T getFieldValue(Object obj, String name, Class<T> fieldType) {
    try {
      Class<?> type = obj.getClass();
      Field field = getField(type, name);
      field.setAccessible(true);
      return (T) field.get(obj);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
