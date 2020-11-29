package listeners.modtools;

import java.util.List;

import config.containers.ServerConfig;
import containers.CommandMessage;
import helpers.Utilities;
import listeners.AbstractMessageListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * listener for the purge command
 * 
 * @author gitterrost4
 */
public class PurgeListener extends AbstractMessageListener {

  /**
   * @param jda
   */
  public PurgeListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getPurgeConfig(), "purge");
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    if (!hasAccess(guild().getMember(event.getAuthor()))) {
      event.getChannel().sendMessage("***You don't have the right to delete messages!***").queue();
      return;
    }
    event.getMessage().delete().complete();
    if (messageContent.getArg(0).filter(x -> x.equals("until")).isPresent()) {
      String untilMessageId = messageContent.getArg(1).orElseThrow(() -> new IllegalStateException("no message id"));
      event.getTextChannel().getHistoryAfter(untilMessageId, 100).queue(history -> {
        List<Message> retrievedHistory = history.getRetrievedHistory();
        Utilities.deleteMessages(event.getTextChannel(), retrievedHistory);
      });
      return;
    }
    try {
      int count = messageContent.getArg(0).map(s -> Integer.parseInt(s))
          .orElseThrow(() -> new IllegalStateException("no argument given"));
      event.getChannel().getHistory().retrievePast(count > 100 ? 100 : count)
          .queue(msgs -> Utilities.deleteMessages(event.getTextChannel(), msgs));
    } catch (NumberFormatException e) {
      error("argument not a number", e);
    }
  }

  @Override
  protected boolean hasAccess(Member member) {
    return member.hasPermission(Permission.MESSAGE_MANAGE);
  }

  @Override
  protected String shortInfoInternal() {
    return "Purge a number of messages from a channel";
  }

  @Override
  protected String usageInternal() {
    return commandString("<COUNT>") + "\n" + commandString("until <MESSAGE_ID>");
  }

  @Override
  protected String descriptionInternal() {
    return "Purge (delete) a number of messages from a channel. You can either specify the number of messages to be purged or a message that you want to purge until (but excluding the specified message).";
  }

  @Override
  protected String examplesInternal() {
    return commandString("10") + "\n" + "Delete the last 10 messages in the channel.\n"
        + commandString("until 1234754073375024") + "\n"
        + "Delete all messages up until (but not including) the message with the id 1234754073375024.";
  }

}
