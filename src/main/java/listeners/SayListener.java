package listeners;

import config.containers.ServerConfigImpl;
import de.gitterrost4.botlib.containers.CommandMessage;
import de.gitterrost4.botlib.listeners.AbstractMessageListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SayListener extends AbstractMessageListener<ServerConfigImpl> {

  public SayListener(JDA jda, Guild guild, ServerConfigImpl config) {
    super(jda, guild, config, config.getSayConfig(), "say");
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    String channel = messageContent.getArgOrThrow(0);
    guild.getTextChannels().stream().filter(c -> channel.replace("!", "").equals(c.getAsMention().replace("!", "")))
        .findFirst().ifPresent(ch -> ch.sendMessage(messageContent.getArgOrThrow(1, true)).queue());
  }

  @Override
  protected boolean hasAccess(Member member) {
    return member.getRoles().stream().anyMatch(r -> guild().getRolesByName(config.getSayConfig().getMinimumRole(), true)
        .stream().anyMatch(r2 -> r.compareTo(r2) >= 0));
  }

}
