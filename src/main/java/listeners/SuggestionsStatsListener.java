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
      this::retrieveReactionCounts, Duration.ofMinutes(30), "Suggestion stats ready");

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

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    MessageChannel channel = event.getChannel();
    Optional<Map<String, Map<String, Long>>> cache = reactionCountCache.get();
    if (!cache.isPresent()) {
      channel.sendMessage("Statistics are not ready yet. Please wait a few minutes.").queue();
    } else {
      cache.ifPresent(m -> {
        Optional<Member> filterMember = event.getMessage().getMentionedMembers().stream().findFirst();
        if (filterMember.isPresent()) {
          EmbedBuilder builder = setEmbedAuthor(new EmbedBuilder(), filterMember.get());
          m.get(filterMember.get().getId()).entrySet().stream()
              .forEach(e -> builder.addField("'" + e.getKey() + "'", e.getValue().toString(), true));
          channel.sendMessage(builder.build()).queue();
        }
      });
    }
  }
}

// end of file
