package listeners;

import java.util.Optional;

import config.containers.ServerConfig;
import containers.CommandMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class AvatarListener extends AbstractMessageListener {

  public AvatarListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, "avatar");
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    Optional<String> userString = messageContent.getArg(0);
    if (userString.isPresent()) {
      userString
          .map(
              String::toLowerCase)
          .map(us -> guild
              .findMembers(m -> m.getEffectiveName().toLowerCase().contains(us) || Optional.ofNullable(m.getNickname())
                  .map(String::toLowerCase).filter(n -> n.contains(us)).isPresent()
                  || m.getUser().getName().toLowerCase().contains(us))
              .onSuccess(memberList -> memberList.stream()
                  .sorted((m1, m2) -> m1.getEffectiveName().toLowerCase().equals(us) ? -1
                      : m2.getEffectiveName().toLowerCase().equals(us) ? 1
                          : m1.getEffectiveName().compareTo(m2.getEffectiveName()))
                  .findFirst().ifPresent(m -> sendAvatar(m, event.getChannel()))));
    } else {
      sendAvatar(event.getMember(), event.getChannel());
    }
  }

  private static void sendAvatar(Member member, MessageChannel channel) {
    channel.sendMessage(setEmbedAuthor(new EmbedBuilder(), member).setTitle("Avatar").setImage(member.getUser().getEffectiveAvatarUrl()+"?size=256").build())
        .queue();
  }

}
