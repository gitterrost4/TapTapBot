package listeners.modtools;

import java.util.List;

import containers.CommandMessage;
import listeners.AbstractMessageListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
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
  public PurgeListener(JDA jda) {
    super(jda, "purge");
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    if (!guild().getMember(event.getAuthor()).hasPermission(Permission.MESSAGE_MANAGE)) {
      event.getChannel().sendMessage("***You don't have the right to delete messages!***").queue();
      return;
    }
    event.getMessage().delete().complete();
    if (messageContent.getArg(0).filter(x -> x.equals("until")).isPresent()) {
      String untilMessageId = messageContent.getArg(1).orElseThrow(() -> new IllegalStateException("no message id"));
      event.getTextChannel().getHistoryAfter(untilMessageId, 100).queue(history -> {
        List<Message> retrievedHistory = history.getRetrievedHistory();
        deleteMessages(event, retrievedHistory);
      });
      return;
    }
    try {
      int count = messageContent.getArg(0).map(s -> Integer.parseInt(s))
          .orElseThrow(() -> new IllegalStateException("no argument given"));
      event.getChannel().getHistory().retrievePast(count > 100 ? 100 : count)
          .queue(msgs -> deleteMessages(event, msgs));
    } catch (NumberFormatException e) {
      throw new IllegalStateException("argument not a number", e);
    }
  }

  private static void deleteMessages(MessageReceivedEvent event, List<Message> retrievedHistory) {
//    retrievedHistory.add(event.getMessage());
    if (retrievedHistory.size() > 1) {
      event.getTextChannel().deleteMessages(retrievedHistory).queue();
    } else {
      retrievedHistory.forEach(msg -> msg.delete().queue());
    }
  }

}
