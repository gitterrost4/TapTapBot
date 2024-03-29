package de.gitterrost4.taptapbot.listeners;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gitterrost4.botlib.containers.CommandMessage;
import de.gitterrost4.botlib.helpers.Emoji;
import de.gitterrost4.botlib.listeners.AbstractMessageListener;
import de.gitterrost4.taptapbot.config.containers.ServerConfig;
import de.gitterrost4.taptapbot.containers.Suggestion;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
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
public class SuggestionsListener extends AbstractMessageListener<ServerConfig> {
  public SuggestionsListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getSuggestionsConfig(), "suggest");
    Timer t = new Timer();
    t.scheduleAtFixedRate(new SuggestionCollector(), 10000,
        1000 * config.getSuggestionsConfig().getTopUpdateIntervalSeconds());
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
        .filter(suggestion -> suggestion.timestamp.isAfter(
            Instant.now().minus(Duration.ofSeconds(config.getSuggestionsConfig().getMaxSuggestionsTimeoutSeconds()))))
        .filter(suggestion -> suggestion.userId.equals(author.getId()))
        .count() >= config.getSuggestionsConfig().getMaxSuggestionsPerUser()) {
      channel.sendMessage("You have sent more than " + config.getSuggestionsConfig().getMaxSuggestionsPerUser()
          + " suggestions in the last " + config.getSuggestionsConfig().getMaxSuggestionsTimeoutSeconds()
          + " seconds. Please wait a bit.").queue();
    } else {
      TextChannel suggestionsChannel = jda.getTextChannelById(config.getSuggestionsConfig().getChannelId());
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
    Optional<String> oldMessage = connectionHelper.getFirstResult(
        "select message from messagecache where messageid = ? and channelid=?", rs -> rs.getString("message"),
        event.getMessageId(), event.getChannel().getId());
    oldMessage.ifPresent(oldMsg -> {
      if (!startingWithPrefix(oldMsg).isPresent()) {
        MessageChannel channel = event.getChannel();
        User author = event.getAuthor();
        Message message = event.getMessage();
        postSuggestion(messageContent, channel, author, message);
      }
    });
  }

  @Override
  public void messageReactionAdd(MessageReactionAddEvent event) {
    Logger logger = LoggerFactory.getLogger(SuggestionsListener.class);
    logger.info("Calling mRA; event.getUser:"+event.getUser().getName());
    if (event.getChannel().getId().equals(config.getSuggestionsConfig().getChannelId())) {
      logger.info("mRA - correct channel");
      event.getTextChannel().retrieveMessageById(event.getMessageId()).queue(message -> {
        logger.info("mRA - retrieved message");
        if (event.getReactionEmote().isEmoji()
            && event.getReactionEmote().getEmoji().equals(new String(Character.toChars(0x1f5d1)))) {
          logger.info("mRA - isEmoji trash");
          if (message.getContentRaw().startsWith("Suggested by: " + event.getMember().getUser().getAsMention())) {
            event.getChannel().deleteMessageById(event.getMessageId()).queue();
          } else {
            event.getReaction().removeReaction(event.getUser()).queue();
          }
        }
        //this broke for some reason and I don't know why
//        if (event.getReactionEmote().isEmoji()
//            && event.getReactionEmote().getEmoji().equals(new String(Character.toChars(0x1F44D)))) {
//          logger.info("mRA - isEmoji 0x1F44D");
//          message.removeReaction("U+1F44E", event.getUser()).queue();
//        }
//        if (event.getReactionEmote().isEmoji()
//            && event.getReactionEmote().getEmoji().equals(new String(Character.toChars(0x1F44E)))) {
//          logger.info("mRA - isEmoji 0x1F44E");
//          message.getReactions();
//          message.removeReaction("U+1F44D", event.getUser()).queue();
//        }
      });
    }
  }

  private void getTopListSuggestions(String topListChannelId, Comparator<? super Message> comparator,
      Predicate<? super Message> filter, boolean all) {
    MessageHistory history = guild().getTextChannelById(config.getSuggestionsConfig().getChannelId()).getHistory();

    List<Message> allMessages = new ArrayList<>();
    List<Message> messages;
    filter = Optional.ofNullable(filter).orElse(m -> true);
    while (!(messages = history.retrievePast(100).complete()).isEmpty()) {
      allMessages.addAll(messages);
    }
    Stream<Message> messageStream = allMessages.stream()
        .filter(m -> m.getAuthor().getId().equals(jda.getSelfUser().getId())).filter(filter).sorted(comparator);
    if (!all) {
      messageStream = messageStream.limit(config.getSuggestionsConfig().getTopCount());
    }
    List<Message> topMessages = messageStream.collect(Collectors.toList());
    List<Message> oldTopMessages = guild().getTextChannelById(topListChannelId).getHistory().retrievePast(100)
        .complete();
    // delete unneeded old messages
    for (int i = topMessages.size(); i < oldTopMessages.size(); i++) {
      oldTopMessages.get(i).delete().queue();
    }
    for (int i = topMessages.size() - 1; i >= 0; i--) {
      if (i < oldTopMessages.size()) {
        oldTopMessages.get(i).editMessageEmbeds(getTopMessageString(topMessages.get(i), i + 1)).queue();
      } else {
        guild().getTextChannelById(topListChannelId).sendMessageEmbeds(getTopMessageString(topMessages.get(i), i + 1))
            .queue();
      }
    }
  }

  private static int compareBest(Message m1, Message m2) {
    return Long.valueOf(getReactionCount(m2, Emoji.THUMBSUP) - getReactionCount(m2, Emoji.THUMBSDOWN))
        .compareTo(Long.valueOf(getReactionCount(m1, Emoji.THUMBSUP) - getReactionCount(m1, Emoji.THUMBSDOWN)));
  }

  private static int compareWorst(Message m1, Message m2) {
    return Long.valueOf(getReactionCount(m2, Emoji.THUMBSDOWN) - getReactionCount(m2, Emoji.THUMBSUP))
        .compareTo(Long.valueOf(getReactionCount(m1, Emoji.THUMBSDOWN) - getReactionCount(m1, Emoji.THUMBSUP)));
  }

  private static int compareTop(Message m1, Message m2) {
    return Double.valueOf((getReactionCount(m2, Emoji.THUMBSUP) + 1.0) / (getReactionCount(m2, Emoji.THUMBSDOWN) + 1.0))
        .compareTo(
            Double.valueOf((getReactionCount(m1, Emoji.THUMBSUP) + 1.0) / (getReactionCount(m1, Emoji.THUMBSDOWN) + 1.0)));
  }

  private static int compareControversial(Message m1, Message m2) {
    return Optional
        .of(Long.valueOf(Math.abs(getReactionCount(m1, Emoji.THUMBSUP) - getReactionCount(m1, Emoji.THUMBSDOWN))).compareTo(
            Long.valueOf(Math.abs(getReactionCount(m2, Emoji.THUMBSUP) - getReactionCount(m2, Emoji.THUMBSDOWN)))))
        .filter(x -> x != 0).orElseGet(
            () -> Long.valueOf(Math.abs(getReactionCount(m2, Emoji.THUMBSUP) + getReactionCount(m2, Emoji.THUMBSDOWN)))
                .compareTo(
                    Long.valueOf(Math.abs(getReactionCount(m1, Emoji.THUMBSUP) + getReactionCount(m1, Emoji.THUMBSDOWN)))));
  }

  private static boolean filterControversial(Message m) {
    return (getReactionCount(m, Emoji.THUMBSUP) + getReactionCount(m, Emoji.THUMBSDOWN)) >= 20;
  }

  private static boolean filterDone(Message m) {
    return getReactionCount(m, Emoji.WHITE_CHECK_MARK) > 0;
  }

  private static int compareId(Message m1, Message m2) {
    return m1.getId().compareTo(m2.getId());
  }

  private static MessageEmbed getTopMessageString(Message m, int place) {
    return new EmbedBuilder().addField("suggestion", m.getContentRaw(), false).addField("Link", m.getJumpUrl(), false)
        .addField("#", "" + place, true)
        .addField("'" + Emoji.THUMBSUP.asString() + "'", "" + getReactionCount(m, Emoji.THUMBSUP), true)
        .addField("'" + Emoji.THUMBSDOWN.asString() + "'", "" + getReactionCount(m, Emoji.THUMBSDOWN), true).build();
  }

  private class SuggestionCollector extends TimerTask {

    @Override
    public void run() {
      getTopListSuggestions(config.getSuggestionsConfig().getTopChannelId(), SuggestionsListener::compareTop, null,
          false);
      getTopListSuggestions(config.getSuggestionsConfig().getBestChannelId(), SuggestionsListener::compareBest, null,
          false);
      getTopListSuggestions(config.getSuggestionsConfig().getWorstChannelId(), SuggestionsListener::compareWorst, null,
          false);
      getTopListSuggestions(config.getSuggestionsConfig().getControversialChannelId(),
          SuggestionsListener::compareControversial, SuggestionsListener::filterControversial, false);
      getTopListSuggestions(config.getSuggestionsConfig().getDoneChannelId(), SuggestionsListener::compareId,
          SuggestionsListener::filterDone, true);
    }
  }

  private static Long getReactionCount(Message message, Emoji emoji) {
    return message.getReactions().stream().filter(r -> r.getReactionEmote().getEmoji().equals(emoji.asString()))
        .findAny().map(r -> r.isSelf() ? r.getCount() - 1l : r.getCount()).orElse(0l);
  }

  @Override
  protected String shortInfoInternal() {
    return "Post suggestions that others can vote on";
  }

  @Override
  protected String usageInternal() {
    return commandString("<SUGGESTION>");
  }

  @Override
  protected String descriptionInternal() {
    return "Post a suggestion to #" + guild().getTextChannelById(config.getSuggestionsConfig().getChannelId()).getName()
        + " and let other members vote on them. \n"
        + "* If you want to include a picture, be sure to attach it to the same message you typed the suggestion in.\n"
        + "* If you want to delete the suggestion again, don't delete what you typed, but click on "
        + Emoji.WASTEBIN.asString() + " beneath the suggestion. Don't forget to vote on your own suggestion.";
  }

  @Override
  protected String examplesInternal() {
    return commandString("Make the game better please.");
  }

}

// end of file
