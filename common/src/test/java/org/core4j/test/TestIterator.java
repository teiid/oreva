package org.core4j.test;

import java.util.Iterator;

import org.core4j.DepthFirstIterator;
import org.core4j.Enumerable;
import org.core4j.Func;
import org.core4j.Func1;

public class TestIterator {

  public static Func1<TreeNode, Enumerable<TreeNode>> GET_CHILDREN = new Func1<TreeNode, Enumerable<TreeNode>>() {
    public Enumerable<TreeNode> apply(TreeNode input) {
      return Enumerable.create(input.nodes);
    }
  };

  private static class TreeNode {

    public final Object value;
    public final TreeNode[] nodes;

    public TreeNode(Object value, TreeNode... nodes) {
      this.value = value;
      this.nodes = nodes;
    }
  }

  public static void main(String[] args) {
    final TreeNode root = new TreeNode("root",
        new TreeNode("a",
            new TreeNode("a.a"),
            new TreeNode("a.b")),
        new TreeNode("b",
            new TreeNode("b.a",
                new TreeNode("b.a.a"))),
        new TreeNode("c"));

    Enumerable<TreeNode> treeEnum = Enumerable.createFromIterator(new Func<Iterator<TreeNode>>() {
      public Iterator<TreeNode> apply() {
        return new DepthFirstIterator<TreeNode>(root, GET_CHILDREN);
      }
    });

    for (TreeNode node : treeEnum) {
      System.out.println(node.value);
    }
  }
}
