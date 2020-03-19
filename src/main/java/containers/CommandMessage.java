// $Id $
// (C) cantamen/Paul Kramer 2019
package containers;

import java.util.Optional;

/**
 * TODO documentation
 */
public class CommandMessage {
  public final String realMessage;

  public CommandMessage(String realMessage) {
    super();
    this.realMessage = realMessage.trim().isEmpty() ? null : realMessage.trim();
  }

  public Optional<String> getArg(int index, boolean untilEnd) {
    try {
      return Optional.ofNullable(realMessage).map(s -> s.split(" +", untilEnd ? index + 1 : 0)[index]);
    } catch (IndexOutOfBoundsException e) {
      return Optional.empty();
    }
  }

  public Optional<String> getArg(int index) {
    return getArg(index, false);
  }

  public boolean hasContent() {
    return realMessage != null;
  }

  @Override
  public String toString() {
    return realMessage;
  }

}

// end of file
