package heap;

import java.util.Optional;
import java.util.function.Consumer;

public class BinomialTreeNode<T extends Comparable<T>> {
  private T element;

  public T getElement () {
    return element;
  }

  private BinomialTreeNode<T>[] children;

  private Optional<BinomialTreeNode<T>> mother;

  private Optional<Consumer<BinomialTreeNode<T>>> propagateRootChange;

  private BinomialHeapHandle<T> handle;

  BinomialHeapHandle<T> getHandle () {
    return handle;
  }

  void setPropagateRootChange (Optional<Consumer<BinomialTreeNode<T>>> propagateRootChange) {
    this.propagateRootChange = propagateRootChange;
  }

  void setPropagateRootChange (Consumer<BinomialTreeNode<T>> propagateRootChange) {
    setPropagateRootChange(Optional.of(propagateRootChange));
  }

  void replaceWithSmallerElement (T element) {
    if (element.compareTo(this.element) > 0)
      throw new RuntimeException("Do you know what 'smaller' means?");
    this.element = element;
    siftUp();
  }

  boolean validTree (Optional<BinomialTreeNode<T>> mother) {
    if(!this.mother.equals(mother))
      throw new RuntimeException();
    Optional<Integer> rankLast = Optional.empty();
    for (int i = 0; i < children.length; i++) {
      if(!children[i].validTree(Optional.of(this)))
        return false;
      if (rankLast.isPresent() && children[i].rank() <= rankLast.get())
        return false;
      if(children[i].getElement().compareTo(element) < 0)
        return false;
      rankLast = Optional.of(children[i].rank());
    }
    return true;
  }

  /**
   * Returns a set of subtrees into which the current tree breaks up
   * when the node of the minimal element is removed.
   * 
   * @return the amount of subtrees
   */
  BinomialTreeNode<T>[] deleteMin () {
    for (int i = 0; i < children.length; i++)
      children[i].mother = Optional.empty();
    return children;
  }

  @SuppressWarnings("unchecked") public BinomialTreeNode (T element) {
    this.children = new BinomialTreeNode[0];
    this.element = element;
    this.mother = Optional.empty();
    this.handle = new BinomialHeapHandle<T>(this);
  }

  /**
   * Determines the minimum element in the subtree.
   * 
   * @return the minimum element
   */
  public T min () {
    return element;
  }

  /**
   * Returns the rank of the subtree
   * 
   * @return rank of the subtree
   */
  public int rank () {
    return children.length;
  }

  /**
   * This method repairs the tree from the current node upwards if
   * a key has decreased.
   */
  private void siftUp () {
    if (!mother.isPresent()) {
      /*
       * If there is no mother node, we are at the root of the tree.
       * We therefore propagate the change to the pile.
       */
      propagateRootChange.get().accept(this);
      return;
    }
    if (mother.get().element.compareTo(this.element) > 0) {
      /*
       * If our element is smaller than the mother's, we exchange it
       * for her element. We also make sure that the handles continue
       * to point to the correct nodes. Finally, we continue with the
       * repair at the mother.
       */
      
      T elementThis = this.element;
      BinomialHeapHandle<T> handleThis = handle;

      handle.setNode(mother.get());
      mother.get().handle.setNode(this);

      this.handle = mother.get().handle;
      this.element = mother.get().element;

      mother.get().element = elementThis;
      mother.get().handle = handleThis;

      mother.get().siftUp();
    }
  }

  /**
   * This method combines two trees of the same rank.
   * 
   * @param a the first tree
   * @param b the second tree
   *
   * @return the one of the two trees to which the other was attached
   */
  public static <T extends Comparable<T>> BinomialTreeNode<T> merge (BinomialTreeNode<T> a, BinomialTreeNode<T> b) {
    if (a.children.length != b.children.length)
      throw new RuntimeException("Unable to merge trees of different rank!");
    @SuppressWarnings("unchecked")
    BinomialTreeNode<T>[] children = new BinomialTreeNode[a.children.length + 1];
    BinomialTreeNode<T> bigger;
    BinomialTreeNode<T> smaller;
    if (a.element.compareTo(b.element) <= 0) {
      smaller = a;
      bigger = b;
    } else {
      smaller = b;
      bigger = a;
    }
    children[children.length - 1] = bigger;
    for (int i = 0; i < children.length - 1; i++)
      children[i] = smaller.children[i];
    smaller.children = children;
    bigger.mother = Optional.of(smaller);
    return smaller;
  }
}
