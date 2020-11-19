package listeners;

import java.util.Optional;

import config.containers.ServerConfig;
import containers.CommandMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class AvatarListener extends AbstractMessageListener {

  public AvatarListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, "avatar");
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    Member member = messageContent.getArg(0).flatMap(us->guild().getMemberCache().applyStream(stream->stream.filter(m -> 
            m.getEffectiveName().toLowerCase().contains(us) || 
            Optional.ofNullable(m.getNickname()).map(String::toLowerCase).filter(n -> n.contains(us)).isPresent()|| 
            m.getUser().getName().toLowerCase().contains(us))
        .sorted((m1, m2) -> m1.getEffectiveName().toLowerCase().equals(us) ? -1
            : m2.getEffectiveName().toLowerCase().equals(us) ? 1
                : m1.getEffectiveName().compareTo(m2.getEffectiveName()))
        .findFirst()))
      .orElseGet(()->event.getMember());
    event.getChannel().sendMessage(setEmbedAuthor(new EmbedBuilder(), member).setTitle("Avatar").setImage(member.getUser().getEffectiveAvatarUrl()+"?size=256").build())
    .queue();
  }

}
