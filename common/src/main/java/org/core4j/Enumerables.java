package org.core4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Iterator;

public class Enumerables {

  public static Enumerable<String> lines(final InputStream stream) {
    final ThrowingFunc<Reader> source = new ThrowingFunc<Reader>() {
      public Reader apply() throws Exception {
        return new InputStreamReader(stream);
      }
    };
    return Enumerable.createFromIterator(new Func<Iterator<String>>() {
      public Iterator<String> apply() {
        return new ReaderLinesIterator(source);
      }
    });
  }

  public static Enumerable<Character> chars(String value) {
    return chars(value.toCharArray());
  }

  public static Enumerable<Character> chars(char[] chars) {
    Character[] rt = new Character[chars.length];
    for (int i = 0; i < chars.length; i++) {
      rt[i] = chars[i];
    }
    return Enumerable.create(rt);
  }

  public static Enumerable<String> lines(final URL url) {
    final ThrowingFunc<Reader> source = new ThrowingFunc<Reader>() {
      public Reader apply() throws Exception {
        return new InputStreamReader(url.openStream());
      }
    };
    return Enumerable.createFromIterator(new Func<Iterator<String>>() {
      public Iterator<String> apply() {
        return new ReaderLinesIterator(source);
      }
    });
  }

  public static Enumerable<String> lines(final File file) {
    final ThrowingFunc<Reader> source = new ThrowingFunc<Reader>() {
      public Reader apply() throws Exception {
        return new FileReader(file);
      }
    };
    return Enumerable.createFromIterator(new Func<Iterator<String>>() {
      public Iterator<String> apply() {
        return new ReaderLinesIterator(source);
      }
    });
  }

  private static class ReaderLinesIterator extends ReadOnlyIterator<String> {

    private final ThrowingFunc<Reader> readerSource;

    public ReaderLinesIterator(ThrowingFunc<Reader> readerSource) {
      this.readerSource = readerSource;
    }

    private BufferedReader reader;

    @Override
    protected IterationResult<String> advance() throws Exception {
      if (reader == null) {
        reader = new BufferedReader(readerSource.apply());
      }
      String line = reader.readLine();
      if (line == null) {
        reader.close();
        return IterationResult.done();
      }
      return IterationResult.next(line);
    }
  }
}
