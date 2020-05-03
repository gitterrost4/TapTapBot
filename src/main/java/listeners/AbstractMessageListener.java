// $Id $
// (C) cantamen/Paul Kramer 2019
package listeners;

import java.util.function.BiConsumer;

import config.Config;
import containers.CommandMessage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;

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

  protected void messageUpdate(MessageUpdateEvent event, CommandMessage messageContent) {
    // do nothing by default
  };

  @Override
  protected final void messageReceived(MessageReceivedEvent event) {
    handleEvent(event, event.getMessage().getContentRaw(), (e, c) -> messageReceived(e, c));
  }

  @Override
  protected final void messageUpdate(MessageUpdateEvent event) {
    handleEvent(event, event.getMessage().getContentRaw(), (e, c) -> messageUpdate(e, c));
  }

  private final <T extends GenericMessageEvent> void handleEvent(T event, String messageContent,
      BiConsumer<T, CommandMessage> consumer) {
    if (isStartingWithPrefix(messageContent)) {
      String realMessageContent = messageContent.replaceFirst("(?i)" + PREFIX + command + " ?\n?", "");
      consumer.accept(event, new CommandMessage(realMessageContent));
    }

  }

  protected boolean isStartingWithPrefix(String messageContent) {
    return messageContent.toLowerCase().startsWith((PREFIX + command + " ").toLowerCase())
        || messageContent.toLowerCase().startsWith((PREFIX + command + "\n").toLowerCase())
        || messageContent.toLowerCase().equals(PREFIX + command);
  }

}

// end of file
