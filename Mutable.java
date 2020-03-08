/**
 * Simple wrapper for objects
 *
 * @param <T> type of objects
 */
public class Mutable<T> {
  private T wrapped;
  
  public T get () {
    return wrapped;
  }
  
  public void set (T wrapped) {
    this.wrapped = wrapped;
  }
  
  public Mutable (T wrapped) {
    this.wrapped = wrapped;
  }
}
