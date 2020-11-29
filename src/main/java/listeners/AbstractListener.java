// $Id $
// (C) cantamen/Paul Kramer 2020
package listeners;

import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.containers.ServerConfig;
import config.containers.modules.ModuleConfig;
import database.ConnectionHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * TODO documentation
 */
public abstract class AbstractListener extends ListenerAdapter {

  protected final JDA jda;
  protected final Guild guild;
  protected final ServerConfig config;
  protected final ModuleConfig moduleConfig;
  protected final ConnectionHelper connectionHelper;

  public AbstractListener(JDA jda, Guild guild, ServerConfig config, ModuleConfig moduleConfig, String databaseFileName) {
    super();
    this.jda = jda;
    this.guild = guild;
    this.config = config;
    this.moduleConfig = moduleConfig;
    this.connectionHelper = new ConnectionHelper(
        Optional.ofNullable(databaseFileName).orElseGet(() -> config.getDatabaseFileName()));
    info("Initializing handler");
  }

  public AbstractListener(JDA jda, Guild guild, ServerConfig config, ModuleConfig moduleConfig) {
    this(jda, guild, config, moduleConfig, null);
  }

  @Override
  public final void onMessageReceived(MessageReceivedEvent event) {
    super.onMessageReceived(event);
    if (event.getAuthor().isBot()) {
      return;
    }
    
    if(!event.isFromGuild()) {
      return;
    }

    if (!event.getGuild().getId().equals(guild.getId())) {
      return;
    }
    if (event.isWebhookMessage()) {
      return;
    }

    messageReceived(event);

  }

  @Override
  public final void onPrivateMessageReactionAdd(PrivateMessageReactionAddEvent event) {
    super.onPrivateMessageReactionAdd(event);
    if (event.getUser().isBot()) {
      return;
    }
    privateMessageReactionAdd(event);
  }

  @Override
  public final void onMessageReactionAdd(MessageReactionAddEvent event) {
    super.onMessageReactionAdd(event);
    if (event.getUser().isBot()) {
      return;
    }
    if (event.getChannelType() == ChannelType.PRIVATE) {
      return;
    }
    if (!event.getGuild().getId().equals(guild.getId())) {
      return;
    }
    messageReactionAdd(event);
  };

  @Override
  public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
    super.onMessageReactionRemove(event);
    if (event.getUser().isBot()) {
      return;
    }
    if (event.getChannelType() == ChannelType.PRIVATE) {
      return;
    }
    if (!event.getGuild().getId().equals(guild.getId())) {
      return;
    }
    messageReactionRemove(event);
  }

  @Override
  public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
    super.onGuildMessageReceived(event);
    if (!event.getGuild().getId().equals(guild.getId())) {
      return;
    }
    if (Optional.ofNullable(event).map(GuildMessageReceivedEvent::getMember).map(Member::getUser).map(User::isBot)
        .orElse(false)) {
      return;
    }
    if (event.isWebhookMessage()) {
      return;
    }
    guildMessageReceived(event);
  }

  @Override
  public void onMessageUpdate(MessageUpdateEvent event) {
    super.onMessageUpdate(event);
    if (!event.getGuild().getId().equals(guild.getId())) {
      return;
    }
    if (Optional.ofNullable(event).map(MessageUpdateEvent::getMember).map(Member::getUser).map(User::isBot)
        .orElse(false)) {
      return;
    }
    messageUpdate(event);
  }

  /**
   * @param event
   *        event object
   */
  protected void messageReactionAdd(MessageReactionAddEvent event) {
    // do nothing by default
  }

  /**
   * @param event
   *        event object
   */
  protected void messageReactionRemove(MessageReactionRemoveEvent event) {
    // do nothing by default
  }

  /**
   * @param event
   *        event object
   */
  protected void privateMessageReactionAdd(PrivateMessageReactionAddEvent event) {
    // do nothing by default
  }

  /**
   * @param event
   *        event object
   */
  protected void messageReceived(MessageReceivedEvent event) {
    // do nothing by default
  }

  /**
   * @param event
   *        event object
   */
  protected void messageUpdate(MessageUpdateEvent event) {
    // do nothing by default
  }

  /**
   * @param event
   *        event object
   */
  protected void guildMessageReceived(GuildMessageReceivedEvent event) {
    // do nothing by default
  }

  protected static EmbedBuilder setEmbedAuthor(EmbedBuilder builder, User author) {
    Optional.ofNullable(author).map(a -> builder.setAuthor(a.getName(), null, a.getEffectiveAvatarUrl()));
    return builder;
  }

  public static EmbedBuilder setEmbedAuthor(EmbedBuilder builder, Member author) {
    Optional.ofNullable(author)
        .map(a -> builder.setAuthor(a.getEffectiveName(), null, a.getUser().getEffectiveAvatarUrl()));
    return builder;
  }
  
  public static EmbedBuilder addLongField(EmbedBuilder builder, String name, String content) {
    while(content.length()>1024) {
      String tmp = content.substring(0, 1024);
      int spaceIndex = tmp.lastIndexOf(" ");
      builder.addField(name, content.substring(0, spaceIndex), false);
      content = content.substring(spaceIndex+1);
      name="";
    }
    builder.addField(name, content, false);
    return builder;
  }

  public Guild guild() {
    return guild;
  }

  protected Logger getLogger() {
    return LoggerFactory.getLogger(this.getClass());
  };

  protected void debug(String message, Object... arguments) {
    getLogger().debug(guild() + " - " + message, arguments);
  }

  protected void info(String message, Object... arguments) {
    getLogger().info(guild() + " - " + message, arguments);
  }

  protected void warn(String message, Object... arguments) {
    getLogger().warn(guild() + " - " + message, arguments);
  }

  protected void error(String message, Object... arguments) {
    getLogger().error(guild() + " - " + message, arguments);
  }

  protected Member getMemberFromSearchString(Optional<String> userString, Supplier<Member> otherwise) {
    return userString.map(String::toLowerCase).map(x->x.replace("!","")).flatMap(us->guild().getMemberCache().applyStream(stream->stream.filter(m -> 
            m.getAsMention().toLowerCase().replace("!","").contains(us) ||
            m.getEffectiveName().toLowerCase().contains(us) || 
            Optional.ofNullable(m.getNickname()).map(String::toLowerCase).filter(n -> n.contains(us)).isPresent()|| 
            m.getUser().getName().toLowerCase().contains(us))
        .sorted((m1, m2) -> m1.getAsMention().replace("!","").toLowerCase().equals(us)?-1:m1.getEffectiveName().toLowerCase().equals(us) ? -1
            : m2.getEffectiveName().toLowerCase().equals(us) ? 1
                : m1.getEffectiveName().compareTo(m2.getEffectiveName()))
        .findFirst()))
      .orElseGet(otherwise);
  }
}

// end of file
