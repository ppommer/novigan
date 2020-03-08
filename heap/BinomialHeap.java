package heap;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Optional;

public class BinomialHeap<T extends Comparable<T>> {
  private ArrayList<BinomialTreeNode<T>> trees;

  private Optional<BinomialTreeNode<T>> minimum;

  private int size;

  public int getSize() {
    return size;
  }

  /**
   * This method adds two binomial trees and one carry tree together.
   * Each of the three trees can be non-existent by 0.
   * 
   * @param a first tree
   * @param b second tree
   * @param carry carry tree
   *
   * @return addition result (1) and a new carry tree (2)
   */
  private Pair<Optional<BinomialTreeNode<T>>, Optional<BinomialTreeNode<T>>> add(
      Optional<BinomialTreeNode<T>> a, Optional<BinomialTreeNode<T>> b,
      Optional<BinomialTreeNode<T>> carry) {
    @SuppressWarnings("unchecked")
    Optional<BinomialTreeNode<T>>[] nodes =
        (Optional<BinomialTreeNode<T>>[]) Array.newInstance(Optional.class, 3);
    for (int i = 0; i < nodes.length; i++)
      nodes[i] = Optional.empty();
    int count = 0;
    if (a.isPresent())
      nodes[count++] = a;
    if (b.isPresent())
      nodes[count++] = b;
    if (carry.isPresent())
      nodes[count++] = carry;
    if (count <= 1)
      return new Pair<>(nodes[0], Optional.empty());
    else {
      assert (nodes[0].get().rank() == nodes[1].get().rank());
      return new Pair<>(nodes[2],
          Optional.of(BinomialTreeNode.merge(nodes[0].get(), nodes[1].get())));
    }
  }

  /**
   * This method is called when a tree has been changed by the
   * replaceWithSmallerElement() operation.
   * 
   * @param index of the tree
   * @param node root of the tree
   */
  private void updateTree(int index, BinomialTreeNode<T> node) {
    trees.set(index, node);
    if (node.getElement().compareTo(minimum.get().getElement()) < 0)
      minimum = Optional.of(node);
  }

  /**
   * This method combines a set of binomial trees sorted by rank
   * with the current cluster.
   * 
   * @param heads amount of binomial trees to be merged
   */
  private void merge(BinomialTreeNode<T>[] heads, boolean ignoreMinimum) {
    Optional<BinomialTreeNode<T>> minimum = Optional.empty();
    ArrayList<BinomialTreeNode<T>> treesNew = new ArrayList<BinomialTreeNode<T>>();
    Optional<BinomialTreeNode<T>> carry = Optional.empty();
    int rank = 0;
    int treeIndex = 0;
    int headsIndex = 0;
    while (true) {
      Optional<BinomialTreeNode<T>> a = Optional.empty();
      int minRank = 0;
      /*
       * First, we check if there is a tree in the cluster of 'rank'.
       * The loop is used to skip the minimum tree if it should be removed.
       */
      while (treeIndex < trees.size()) {
        BinomialTreeNode<T> tree = trees.get(treeIndex);
        if (ignoreMinimum && tree == this.minimum.get()) {
          treeIndex++;
          continue;
        }
        minRank = tree.rank();
        if (tree.rank() == rank) {
          a = Optional.of(tree);
          treeIndex++;
        }
        break;
      }
      /*
       * Next, we check if there is a tree in 'heads' of 'rank' and update
       * the minimum rank 'minRank' accordingly.
       */
      Optional<BinomialTreeNode<T>> b = Optional.empty();
      if (headsIndex < heads.length) {
        BinomialTreeNode<T> tree = heads[headsIndex];
        if (tree.rank() < minRank)
          minRank = tree.rank();
        if (tree.rank() == rank) {
          b = Optional.of(tree);
          headsIndex++;
        }
      }
      if (!a.isPresent() && !b.isPresent() && !carry.isPresent()) {
        if (treeIndex >= trees.size() && headsIndex >= heads.length)
          /*
           * If there are no trees in the heap, no trees in heads and no carry,
           * we are done.
           */
          break;
        else {
          /*
           * A gap in the ranks, which exists both in the heap and in heads,
           * can be skipped.
           */
          rank = minRank;
          continue;
        }
      } else
        rank++;
      /*
       * We calculate the next tree and the carry tree.
       */
      Pair<Optional<BinomialTreeNode<T>>, Optional<BinomialTreeNode<T>>> result = add(a, b, carry);
      Optional<BinomialTreeNode<T>> x = result._1;
      carry = result._2;
      if (x.isPresent()) {
        /*
         * We may have to update the minimum of the new tree.
         */
        if (!minimum.isPresent() || x.get().getElement().compareTo(minimum.get().getElement()) < 0)
          minimum = x;
        final int sizeCurrent = treesNew.size();
        /*
         * We give the subtree a callback, which is called by the replaceWithSmallerElement()
         * operation on changes.
         */
        x.get().setPropagateRootChange(node -> updateTree(sizeCurrent, node));
        treesNew.add(x.get());
      }
    }
    this.trees = treesNew;
    this.minimum = minimum;
  }

  @SuppressWarnings("unused")
  private boolean validHeap() {
    if (trees.isEmpty())
      return true;
    T minE = peek();
    Optional<Integer> rankLast = Optional.empty();
    for (int i = 0; i < trees.size(); i++) {
      if (!trees.get(i).validTree(Optional.empty()))
        return false;
      if (rankLast.isPresent() && trees.get(i).rank() <= rankLast.get())
        return false;
      if (trees.get(i).getElement().compareTo(minE) < 0)
        return false;
      rankLast = Optional.of(trees.get(i).rank());
    }
    return true;
  }

  /**
   * This constructor builds an empty heap.
   */
  public BinomialHeap() {
    trees = new ArrayList<>();
    minimum = Optional.empty();
    size = 0;
  }

  /**
   * This method adds an element to the heap.
   * 
   * @param element to be added
   */
  @SuppressWarnings("unchecked")
  public Object insert(T element) {
    BinomialTreeNode<T> node = new BinomialTreeNode<T>(element);
    merge(new BinomialTreeNode[] {node}, false);
    size++;
    return node.getHandle();
  }

  /**
   * This method determines the minimum element in the binomial heap.
   * 
   * @return the minimum element
   */
  public T peek() {
    if (!minimum.isPresent())
      throw new RuntimeException("Empty :-(");
    return minimum.get().getElement();
  }

  /**
   * This method removes the minimum element from the binomial heap and
   * returns it.
   * 
   * @return the minimum element
   */
  public T poll() {
    T min = peek();
    BinomialTreeNode<T> minimumTree = minimum.get();
    BinomialTreeNode<T>[] children = minimumTree.deleteMin();
    merge(children, true);
    size--;
    // if(!validHeap())
    // throw new RuntimeException("Invalid tree!");
    return min;
  }

  public void replaceWithSmallerElement(Object handle, T elementNew) {
    @SuppressWarnings("unchecked")
    BinomialHeapHandle<T> handleCasted = (BinomialHeapHandle<T>) handle;
    handleCasted.getNode().replaceWithSmallerElement(elementNew);
    // if(!validHeap())
    // throw new RuntimeException("Invalid tree!");
  }
}
