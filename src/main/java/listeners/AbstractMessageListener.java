// $Id $
// (C) cantamen/Paul Kramer 2019
package listeners;

import config.Config;
import containers.CommandMessage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * TODO documentation
 *
 * @author (C) cantamen/Paul Kramer 2019
 * @version $Id $
 */
public abstract class AbstractMessageListener extends AbstractListener {

  protected static String PREFIX = Config.get("bot.prefix");
  private final String command;

  public AbstractMessageListener(JDA jda, String command) {
    super(jda);
    this.command = command;
  }

  protected abstract void messageReceived(MessageReceivedEvent event, CommandMessage messageContent);

  @Override
  protected final void messageReceived(MessageReceivedEvent event) {
    String messageContent = event.getMessage().getContentRaw();
    if (messageContent.toLowerCase().startsWith((PREFIX + command + " ").toLowerCase())
        || messageContent.toLowerCase().equals(PREFIX + command)) {
      String realMessageContent = messageContent.replaceFirst("(?i)" + PREFIX + command + " ?", "");
      messageReceived(event, new CommandMessage(realMessageContent));
    }
  };

}

// end of file
