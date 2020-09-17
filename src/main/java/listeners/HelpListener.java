package listeners;

import java.util.Optional;
import java.util.stream.Collectors;

import config.containers.ServerConfig;
import containers.CommandMessage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HelpListener extends AbstractMessageListener {

  private ListenerManager listenerManager;

  public HelpListener(JDA jda, Guild guild, ServerConfig config, ListenerManager listenerManager) {
    super(jda, guild, config, "help");
    this.listenerManager = listenerManager;
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    Optional<String> command = messageContent.getArg(0);
    Member member = event.getMember();
    if (!command.isPresent()) {
      event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage(getOverview(member)).queue());
    } else {
      getCommandHelp(command.get(), member).ifPresent(
          message -> event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage(message).queue()));
    }
  }

  private String getOverview(Member member) {
    return listenerManager.getListeners().stream().filter(listener -> listener instanceof AbstractMessageListener)
        .map(listener -> (AbstractMessageListener) listener).map(listener -> listener.shortInfo(member))
        .filter(Optional::isPresent).map(Optional::get).collect(Collectors.joining("\n"));
  }

  private Optional<String> getCommandHelp(String command, Member member) {

    return listenerManager.getListeners().stream().filter(listener -> listener instanceof AbstractMessageListener)
        .map(listener -> (AbstractMessageListener) listener).filter(listener -> listener.command.equals(command))
        .findFirst().flatMap(listener -> listener.help(member));
  }

}