package helpers;

@FunctionalInterface
public interface RunnableWithThrowable<T extends Throwable> {
  public void run() throws T;
}
