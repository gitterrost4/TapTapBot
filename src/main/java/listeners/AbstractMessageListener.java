// $Id $
// (C) cantamen/Paul Kramer 2019
package listeners;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import config.containers.ServerConfig;
import config.containers.modules.CommandModuleConfig;
import containers.CommandMessage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
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

  private List<String> PREFIXES = config.getBotPrefixes();
  protected final String command;
  private final String commandSeparator;

  public AbstractMessageListener(JDA jda, Guild guild, ServerConfig config, CommandModuleConfig moduleConfig,
      String command) {
    this(jda, guild, config, moduleConfig, command, " +");
  }

  public AbstractMessageListener(JDA jda, Guild guild, ServerConfig config, CommandModuleConfig moduleConfig,
      String command, String commandSeparator) {
    super(jda, guild, config, moduleConfig);
    this.command = command;
    this.commandSeparator = commandSeparator;
  }

  public AbstractMessageListener(JDA jda, Guild guild, ServerConfig config, CommandModuleConfig moduleConfig,
      String command, String commandSeparator, String databaseFileName) {
    super(jda, guild, config, moduleConfig, databaseFileName);
    this.command = command;
    this.commandSeparator = commandSeparator;
  }

  protected abstract void messageReceived(MessageReceivedEvent event, CommandMessage messageContent);

  /**
   * @param event
   * @param messageContent
   */
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
    if (((CommandModuleConfig) moduleConfig).getRestrictToChannels() == null
        || ((CommandModuleConfig) moduleConfig).getRestrictToChannels().isEmpty()
        || ((CommandModuleConfig) moduleConfig).getRestrictToChannels().contains(event.getChannel().getId())) {
      startingWithPrefix(messageContent).ifPresent(prefix -> {
        String realMessageContent = messageContent.replaceFirst("(?i)" + Pattern.quote(prefix) + command + " ?\n?", "");
        info("Invoked command {}", prefix + command);
        consumer.accept(event, new CommandMessage(realMessageContent, commandSeparator));
      });
    }
  }

  protected Optional<String> startingWithPrefix(String messageContent) {
    return PREFIXES.stream()
        .filter(prefix -> messageContent.toLowerCase().startsWith((prefix + command + " ").toLowerCase())
            || messageContent.toLowerCase().startsWith((prefix + command + "\n").toLowerCase())
            || messageContent.toLowerCase().equals(prefix + command))
        .findFirst();
  }

  /**
   * does the user have access to this command?
   * 
   * @param member
   * @return
   */
  protected boolean hasAccess(Member member) {
    return true;
  }

  /**
   * @return Short info text describing what the command does
   */
  protected String shortInfoInternal() {
    return null;
  }

  /**
   * @return the help text for this command
   */
  protected String helpInternal() {
    return Optional
        .of(Optional.ofNullable(shortInfoInternal()).map(s -> "*" + s + "*\n").orElse("")
            + Optional.ofNullable(usageInternal()).map(s -> "**USAGE:**\n" + s + "\n").orElse("")
            + Optional.ofNullable(descriptionInternal()).map(s -> "**DESCRIPTION**\n" + s + "\n").orElse("")
            + Optional.ofNullable(examplesInternal()).map(s -> "**EXAMPLES**\n" + s + "\n").orElse(""))
        .filter(s -> !s.isEmpty()).orElse(null);
  }

  protected String usageInternal() {
    return null;
  }

  protected String descriptionInternal() {
    return null;
  }

  protected String examplesInternal() {
    return null;
  }

  /**
   * @param member
   * @return the short info for this command or empty optional if the member doesn't
   *         have access to it
   */
  public Optional<String> shortInfo(Member member) {
    return Optional.ofNullable(shortInfoInternal()).map(s -> PREFIXES.get(0) + command + " - " + s)
        .filter(s -> hasAccess(member));
  }

  /**
   * @param member
   * @return the help for this command or empty optional if the member doesn't have
   *         access to it
   */
  public Optional<String> help(Member member) {
    return Optional.ofNullable(helpInternal()).map(s -> "***" + PREFIXES.get(0) + command + "***\n" + s)
        .filter(s -> hasAccess(member));
  }

  protected final String commandString(String argumentString) {
    return "`" + PREFIXES.get(0) + command + " " + argumentString + "`";
  }

}

// end of file
