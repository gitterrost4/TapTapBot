package listeners;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import config.Config;
import containers.CommandMessage;
import containers.WatchMessage;
import database.ConnectionHelper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class WatchListListener extends AbstractMessageListener {

  public WatchListListener(JDA jda) {
    super(jda, "watchlist");
    ConnectionHelper.update(
        "create table if not exists watchmessage(id INTEGER PRIMARY KEY not null, userid varchar(255) not null, channelid varchar(255) not null, messageid varchar(255) not null, message text not null, tmstmp TEXT not null, confirmed tinyint(4) not null default 0);");
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
      return;
    }

    if(messageContent.getArg(0).equals("list")) {
      Map<String,Long> messageCount=WatchMessage.all().stream().collect(Collectors.groupingBy(wm->wm.userId,Collectors.counting()));
      event.getChannel().sendMessage("**List of watched messages per user**\n```\n"+
      messageCount.entrySet().stream().sorted((e1,e2)->e2.getValue().compareTo(e1.getValue())).map(e->String.format("%3d - %s",e.getValue(),event.getGuild().getMemberById(e.getKey()).getUser().getAsTag())).collect(Collectors.joining("\n"))+
      "```").queue();
      return;
    }
    
    if (event.getMessage().getMentionedUsers().size() == 0) {
      event.getChannel().sendMessage("**No user specified**").queue();
      return;
    }
    String userId = event.getMessage().getMentionedUsers().get(0).getId();
    List<WatchMessage> watchMessages = WatchMessage.findWatchMessageByUserId(userId, true);
    if (watchMessages.size() == 0) {
      event.getChannel().sendMessage("**No watched messages for that user**").queue();
    } else {
      event.getChannel().sendMessage("**Watched messages for " + jda.getUserById(userId).getAsTag() + "**")
          .queue(unused -> watchMessages
              .forEach(watchMessage -> event.getChannel().sendMessage(watchMessage.getPrintableString()).queue()));
    }

  }

  @Override
  public void privateMessageReactionAdd(PrivateMessageReactionAddEvent event) {
    if (event.getUser().isBot()) {
      return;
    }
    event.getChannel().retrieveMessageById(event.getMessageId()).queue(message -> {
      if (!message.getAuthor().getId().equals(jda.getSelfUser().getId())) {
        return;
      }
      if (!message.getContentRaw().startsWith("WATCHID:")) {
        return;
      }
      Integer watchMessageId = Integer
          .parseInt(message.getContentRaw().substring(8, message.getContentRaw().indexOf("\n")));
      if (!(event.getReactionEmote().isEmoji()
          && event.getReactionEmote().getEmoji().equals(new String(Character.toChars(0x1f44E))))) {
        // thumbsup
        Optional<WatchMessage> watchMessage = WatchMessage.findWatchMessageById(watchMessageId);
        if (watchMessage.isPresent()) {
          watchMessage.get().confirm();
          event.getChannel().sendMessage("Message put on watchlist").queue();
          jda.getTextChannelById(watchMessage.get().channelId).retrieveMessageById(watchMessage.get().messageId)
              .queue(m -> m.removeReaction("U+1F440", event.getUser()).queue());
        }
      } else if (!(event.getReactionEmote().isEmoji()
          && event.getReactionEmote().getEmoji().equals(new String(Character.toChars(0x1f44D))))) {
        // thumbsdown
        Optional<WatchMessage> watchMessage = WatchMessage.findWatchMessageById(watchMessageId);
        if (watchMessage.isPresent()) {
          watchMessage.get().delete();
        }
      }
      message.delete().queue();
    });

  }

  @Override
  public void messageReactionAdd(MessageReactionAddEvent event) {

    if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
      return;
    }

    if (!(event.getReactionEmote().isEmoji()
        && event.getReactionEmote().getEmoji().equals(new String(Character.toChars(0x1f440))))) {
      return;
    }

    if (WatchMessage.findWatchMessageByChannelIdAndMessageId(event.getChannel().getId(), event.getMessageId())
        .isPresent()) {
      event.getUser().openPrivateChannel()
          .queue(channel -> channel.sendMessage("Message has already been added to the watch list").queue());
      return;
    }

    // send confirmation message to the moderator
    event.getChannel().retrieveMessageById(event.getMessageId()).queue(message -> {
      WatchMessage watchMessage = new WatchMessage(message.getAuthor().getId(), message.getChannel().getId(),
          message.getId(), message.getTimeCreated().toInstant(), message.getContentRaw()).persist();
      event.getUser().openPrivateChannel()
          .queue(channel -> channel
              .sendMessage("WATCHID:" + watchMessage.id + "\nDo you want to put the message by "
                  + message.getAuthor().getAsMention() + " on the watchlist?\n>>> " + message.getContentRaw()
                  + message.getAttachments().stream().map(Attachment::getUrl).map(x -> "\n" + x)
                      .collect(Collectors.joining()))
              .queue(sentMessage -> sentMessage.addReaction("U+1F44D")
                  .queue(unused -> sentMessage.addReaction("U+1F44E").queue())));
    });

    if (event.getChannel().getId().equals(Config.get("suggestions.channelId"))) {
      event.getTextChannel().retrieveMessageById(event.getMessageId()).queue(message -> {
        if (event.getReactionEmote().isEmoji()
            && event.getReactionEmote().getEmoji().equals(new String(Character.toChars(0x1f5d1)))) {
          if (message.getContentRaw().startsWith("Suggested by: " + event.getMember().getUser().getAsMention())) {
            event.getChannel().deleteMessageById(event.getMessageId()).queue();
          } else {
            event.getReaction().removeReaction(event.getUser()).queue();
          }
        }
        if (event.getReactionEmote().isEmoji()
            && event.getReactionEmote().getEmoji().equals(new String(Character.toChars(0x1F44D)))) {
          message.removeReaction("U+1F44E", event.getUser()).queue();
        }
        if (event.getReactionEmote().isEmoji()
            && event.getReactionEmote().getEmoji().equals(new String(Character.toChars(0x1F44E)))) {
          message.removeReaction("U+1F44D", event.getUser()).queue();
        }
      });
    }
  }

}
