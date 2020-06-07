package listeners;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import config.Config;
import containers.CommandMessage;
import containers.Suggestion;
import database.ConnectionHelper;
import helpers.Emoji;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

// $Id $
// (C) cantamen/Paul Kramer 2019

/**
 * Listener for the suggestions module
 */
public class SuggestionsListener extends AbstractMessageListener {
  public SuggestionsListener(JDA jda) {
    super(jda, "suggest");
    Timer t = new Timer();
    t.scheduleAtFixedRate(new SuggestionCollector(), 10000,
        1000 * Config.getInt("suggestions.topUpdateIntervalSeconds"));
  }

  private List<Suggestion> lastSuggestions = new ArrayList<>();

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    MessageChannel channel = event.getChannel();
    User author = event.getAuthor();
    Message message = event.getMessage();
    postSuggestion(messageContent, channel, author, message);
  }

  private void postSuggestion(CommandMessage messageContent, MessageChannel channel, User author, Message message) {
    if (!messageContent.hasContent()) {
      return;
    }
    if (lastSuggestions.stream()
        .filter(suggestion -> suggestion.timestamp.isAfter(Instant.now()
            .minus(Duration.ofSeconds(Integer.parseInt(Config.get("suggestions.maxSuggestionsTimeoutSeconds"))))))
        .filter(suggestion -> suggestion.userId.equals(author.getId()))
        .count() >= Integer.parseInt(Config.get("suggestions.maxSuggestionsPerUser"))) {
      channel.sendMessage(
          "You have sent more than " + Config.get("suggestions.maxSuggestionsPerUser") + " suggestions in the last "
              + Config.get("suggestions.maxSuggestionsTimeoutSeconds") + " seconds. Please wait a bit.")
          .queue();
    } else {
      TextChannel suggestionsChannel = jda.getTextChannelById(Config.get("suggestions.channelId"));
      suggestionsChannel.sendMessage("Suggested by: " + author.getAsMention() + "\n>>> " + messageContent
          + message.getAttachments().stream().map(Attachment::getUrl).map(x -> "\n" + x).collect(Collectors.joining()))
          .queue(success -> {
            success.addReaction("U+1F44D").queue(
                unused -> success.addReaction("U+1F44E").queue(unused2 -> success.addReaction("U+1F5D1").queue()));
            lastSuggestions.add(new Suggestion(author.getId()));
          });
    }
  }

  @Override
  protected void messageUpdate(MessageUpdateEvent event, CommandMessage messageContent) {
    Optional<String> oldMessage = ConnectionHelper.getFirstResult(
        "select message from messagecache where messageid = ? and channelid=?", rs -> rs.getString("message"),
        event.getMessageId(), event.getChannel().getId());
    oldMessage.ifPresent(oldMsg -> {

      if (!isStartingWithPrefix(oldMsg)) {
        MessageChannel channel = event.getChannel();
        User author = event.getAuthor();
        Message message = event.getMessage();
        postSuggestion(messageContent, channel, author, message);
      }
    });
  }

  @Override
  public void messageReactionAdd(MessageReactionAddEvent event) {
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

  private void getTopListSuggestions(String topListChannelId, Comparator<? super Message> comparator) {
    MessageHistory history = guild().getTextChannelById(Config.get("suggestions.channelId")).getHistory();

    List<Message> allMessages = new ArrayList<>();
    List<Message> messages;
    while (!(messages = history.retrievePast(100).complete()).isEmpty()) {
      allMessages.addAll(messages);
    }
    int topCount = Config.getInt("suggestions.topCount");
    List<Message> topMessages = allMessages.stream()
        .filter(m -> m.getAuthor().getId().equals(jda.getSelfUser().getId())).sorted(comparator).limit(topCount)
        .collect(Collectors.toList());
    List<Message> oldTopMessages = guild().getTextChannelById(topListChannelId).getHistory().retrievePast(100)
        .complete();
    // delete unneeded old messages
    for (int i = topMessages.size(); i < oldTopMessages.size(); i++) {
      oldTopMessages.get(i).delete().queue();
    }
    for (int i = topMessages.size() - 1; i >= 0; i--) {
      if (i < oldTopMessages.size()) {
        oldTopMessages.get(i).editMessage(getTopMessageString(topMessages.get(i), i + 1)).queue();
      } else {
        guild().getTextChannelById(topListChannelId).sendMessage(getTopMessageString(topMessages.get(i), i + 1))
            .queue();
      }
    }
  }

  private static int compareBest(Message m1, Message m2) {
    return new Long(getReactionCount(m2, Emoji.THUMBSUP) - getReactionCount(m2, Emoji.THUMBSDOWN))
        .compareTo(new Long(getReactionCount(m1, Emoji.THUMBSUP) - getReactionCount(m1, Emoji.THUMBSDOWN)));
  }

  private static int compareWorst(Message m1, Message m2) {
    return new Long(getReactionCount(m2, Emoji.THUMBSDOWN) - getReactionCount(m2, Emoji.THUMBSUP))
        .compareTo(new Long(getReactionCount(m1, Emoji.THUMBSDOWN) - getReactionCount(m1, Emoji.THUMBSUP)));
  }

  private static int compareTop(Message m1, Message m2) {
    return new Double((getReactionCount(m2, Emoji.THUMBSUP) + 1.0) / (getReactionCount(m2, Emoji.THUMBSDOWN) + 1.0))
        .compareTo(
            new Double((getReactionCount(m1, Emoji.THUMBSUP) + 1.0) / (getReactionCount(m1, Emoji.THUMBSDOWN) + 1.0)));
  }

  private static MessageEmbed getTopMessageString(Message m, int place) {
    return new EmbedBuilder().addField("suggestion", m.getContentRaw(), false).addField("Link", m.getJumpUrl(), false)
        .addField("#", "" + place, true)
        .addField("'"+Emoji.THUMBSUP.asString()+"'", "" + getReactionCount(m, Emoji.THUMBSUP), true)
        .addField("'"+Emoji.THUMBSDOWN.asString()+"'", "" + getReactionCount(m, Emoji.THUMBSDOWN), true).build();
  }

  private class SuggestionCollector extends TimerTask {

    @Override
    public void run() {
      getTopListSuggestions(Config.get("suggestions.topChannelId"), SuggestionsListener::compareTop);
      getTopListSuggestions(Config.get("suggestions.bestChannelId"), SuggestionsListener::compareBest);
      getTopListSuggestions(Config.get("suggestions.worstChannelId"), SuggestionsListener::compareWorst);
    }
  }

  private static Long getReactionCount(Message message, Emoji emoji) {
    return message.getReactions().stream().filter(r -> r.getReactionEmote().getEmoji().equals(emoji.asString()))
        .findAny().map(r -> r.isSelf() ? r.getCount() - 1l : r.getCount()).orElse(0l);
  }
}

// end of file
