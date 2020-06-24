package listeners;

import java.time.Duration;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import config.Config;
import containers.CommandMessage;
import helpers.CachedSupplier;
import helpers.Emoji;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

// $Id $
// (C) cantamen/Paul Kramer 2019

/**
 * Listener for the suggestions module
 */
public class SuggestionsStatsListener extends AbstractMessageListener {
  private CachedSupplier<Map<String, Map<String, Long>>> reactionCountCache = new CachedSupplier<>(
      this::retrieveReactionCounts, Duration.ofMinutes(30), "Collecting suggestion stats...", "Suggestion stats ready");
  private CachedSupplier<Map<String, Map<String, Integer>>> votesOnOwnSuggestionsCountCache = new CachedSupplier<>(
      this::retrieveOwnSuggestionVotesCounts, Duration.ofMinutes(30), "Collecting own suggestion stats...",
      "Own Suggestion stats ready");

  public SuggestionsStatsListener(JDA jda) {
    super(jda, "suggeststats");
  }

  private Map<String, Map<String, Long>> retrieveReactionCounts() {
    MessageHistory history = guild().getTextChannelById(Config.get("suggestions.channelId")).getHistory();
    List<Message> allMessages = new ArrayList<>();
    List<Message> messages;
    while (!(messages = history.retrievePast(100).complete()).isEmpty()) {
      allMessages.addAll(messages);
    }

    return allMessages.stream()
        .flatMap(m -> m.getReactions().stream()
            .flatMap(r -> r.retrieveUsers().complete().stream().map(u -> new SimpleEntry<>(u, r.getReactionEmote()))))
        .collect(Collectors.groupingBy(e -> e.getKey().getId(),
            Collectors.groupingBy(e -> e.getValue().getEmoji(), Collectors.counting())));
  }

  private Map<String, Map<String, Integer>> retrieveOwnSuggestionVotesCounts() {
    MessageHistory history = guild().getTextChannelById(Config.get("suggestions.channelId")).getHistory();
    List<Message> allMessages = new ArrayList<>();
    List<Message> messages;
    while (!(messages = history.retrievePast(100).complete()).isEmpty()) {
      allMessages.addAll(messages);
    }

    return allMessages.stream().filter(m -> m.getMentionedUsers().stream().findAny().isPresent())
        .flatMap(m -> m.getReactions().stream().filter(r->!r.getReactionEmote().getEmoji().equals(Emoji.WASTEBIN.asString()))
            .map(r -> new SimpleEntry<>(m.getMentionedUsers().stream().findFirst().get(),
                new SimpleEntry<>(r.getReactionEmote().getEmoji(), r.getCount()))))
        .collect(Collectors.groupingBy(e -> e.getKey().getId(),
            Collectors.groupingBy(e -> e.getValue().getKey(), Collectors.summingInt(e -> e.getValue().getValue()-1))));
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    MessageChannel channel = event.getChannel();
    Optional<Map<String, Map<String, Long>>> reactionCache = reactionCountCache.get();
    Optional<Map<String, Map<String, Integer>>> ownSuggestionVotesCache = votesOnOwnSuggestionsCountCache.get();
    Member filterMember = event.getMessage().getMentionedMembers().stream().findFirst().orElse(event.getMember());
    EmbedBuilder builder = setEmbedAuthor(new EmbedBuilder(), filterMember);
    if (!reactionCache.isPresent()) {
      builder.addField("Own votes on other suggestions", "Statistics are not ready yet. Please wait a few minutes.", false);      
    } else {
      builder.addField("Own votes on other suggestions", "", false);      
      reactionCache.ifPresent(m -> {
        Optional.ofNullable(m.get(filterMember.getId())).ifPresent(x -> x.entrySet().stream()
            .forEach(e -> builder.addField("'" + e.getKey() + "'", e.getValue().toString(), true)));
      });
    }
    if (!ownSuggestionVotesCache.isPresent()) {
      builder.addField("Other votes on own suggestions", "Statistics are not ready yet. Please wait a few minutes.", false);      
    } else {
      builder.addField("Other votes on own suggestions", "", false);      
      ownSuggestionVotesCache.ifPresent(m -> {
        Optional.ofNullable(m.get(filterMember.getId())).ifPresent(x -> x.entrySet().stream()
            .forEach(e -> builder.addField("'" + e.getKey() + "'", e.getValue().toString(), true)));
      });
    }
    channel.sendMessage(builder.build()).queue();
  }
}

// end of file
