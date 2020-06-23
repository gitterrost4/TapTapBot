package helpers;

import java.time.Duration;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

public class CachedSupplier<T> implements Supplier<Optional<T>>{

  private final Supplier<T> valueSupplier;
  private Optional<T> value = Optional.empty();
  
  public CachedSupplier(Supplier<T> valueSupplier, Duration timeout) {
    super();
    this.valueSupplier = valueSupplier;
    Timer t = new Timer();
    t.scheduleAtFixedRate(new Updater(), 10000,
        timeout.toMillis());

  }

  @Override
  public Optional<T> get() {
    return value;
  }

  private class Updater extends TimerTask {

    @Override
    public void run() {
      value = Optional.of(valueSupplier.get());
    }
  }

}
