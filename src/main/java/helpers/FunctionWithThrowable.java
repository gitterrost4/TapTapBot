package helpers;

@FunctionalInterface
public interface FunctionWithThrowable<S, O, T extends Throwable> {
  public O apply(S s) throws T;
}
