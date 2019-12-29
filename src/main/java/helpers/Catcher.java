package helpers;

import java.util.function.Supplier;

public class Catcher {

  public static <T> T wrap(SupplierWithThrowable<T, Exception> a) {
    try {
      return a.get();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> Supplier<T> wrapSup(SupplierWithThrowable<T, Exception> b) {
    return () -> {
      try {
        return b.get();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }
}
