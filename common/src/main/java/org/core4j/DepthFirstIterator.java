package org.core4j;

import java.util.Stack;

public class DepthFirstIterator<T> extends ReadOnlyIterator<T> {

  private final Func1<T, Enumerable<T>> childrenFn;
  private final Stack<T> stack = new Stack<T>();

  public DepthFirstIterator(T startingNode, Func1<T, Enumerable<T>> childrenFn) {
    this.childrenFn = childrenFn;
    this.stack.add(startingNode);
  }

  @Override
  protected IterationResult<T> advance() throws Exception {

    // first child
    for (T child : childrenFn.apply(stack.peek())) {
      stack.push(child);
      return IterationResult.next(child);
    }

    // no children
    while (stack.size() > 1) {
      T currentNode = stack.pop();

      // look for next sibling
      boolean foundSelf = false;
      for (T sibling : childrenFn.apply(stack.peek())) {
        if (foundSelf) {
          stack.push(sibling);
          return IterationResult.next(sibling);
        }
        if (sibling.equals(currentNode)) {
          foundSelf = true;
        }
      }
      // no sibling found, move up and try again

    }

    return IterationResult.done();
  }
}
