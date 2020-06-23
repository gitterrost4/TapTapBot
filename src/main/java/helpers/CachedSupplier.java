package helpers;

import java.time.Duration;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

public class CachedSupplier<T> implements Supplier<Optional<T>>{

  private final Supplier<T> valueSupplier;
  private Optional<T> value = Optional.empty();
  private final Optional<String> firstInitMessage;
  
  public CachedSupplier(Supplier<T> valueSupplier, Duration timeout, String firstInitMessage) {
    super();
    this.valueSupplier = valueSupplier;
    Timer t = new Timer();
    t.scheduleAtFixedRate(new Updater(), 10000,
        timeout.toMillis());
    this.firstInitMessage=Optional.ofNullable(firstInitMessage);

  }
  
  public CachedSupplier(Supplier<T> valueSupplier, Duration timeout) {
    this(valueSupplier,timeout,null);
  }

  @Override
  public Optional<T> get() {
    return value;
  }

  private class Updater extends TimerTask {

    @Override
    public void run() {
      boolean sendmsg = !value.isPresent();
      value = Optional.of(valueSupplier.get());
      if(sendmsg&&firstInitMessage.isPresent()) {
        System.err.println(firstInitMessage.get());
      }
    }
  }

}
