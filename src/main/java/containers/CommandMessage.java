// $Id $
// (C) cantamen/Paul Kramer 2019
package containers;

import java.util.Optional;

/**
 * TODO documentation
 */
public class CommandMessage {
  public final String realMessage;

  public final String commandSeparator;

  public CommandMessage(String realMessage, String commandSeparator) {
    super();
    this.realMessage = realMessage.trim().isEmpty() ? null : realMessage.trim();
    this.commandSeparator = commandSeparator;
  }

  public Optional<String> getArg(int index, boolean untilEnd) {
    try {
      return Optional.ofNullable(realMessage).map(s -> s.split(commandSeparator, untilEnd ? index + 1 : 0)[index]);
    } catch (@SuppressWarnings("unused") IndexOutOfBoundsException e) {
      return Optional.empty();
    }
  }

  public Optional<String> getArg(int index) {
    return getArg(index, false);
  }

  public String getArgOrThrow(int index) {
    return getArg(index, false).orElseThrow(() -> new IllegalArgumentException("too few parameters"));
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
