package de.gitterrost4.taptapbot.listeners;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.gitterrost4.botlib.listeners.AbstractListener;
import de.gitterrost4.taptapbot.config.containers.ServerConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;

public class MessagePosterListener extends AbstractListener<ServerConfig> {

  public MessagePosterListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getMessagePosterConfig());
  }

  @Override
  public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
    super.onGuildMessageDelete(event);
    Optional<List<String>> oldMessage = connectionHelper.getFirstResult(
        "select message, userid from messagecache where messageid = ? and channelid=?",
        rs -> Stream.of(rs.getString("message"), rs.getString("userid")).collect(Collectors.toList()),
        event.getMessageId(), event.getChannel().getId());
    if(oldMessage.isPresent()) {
      if(oldMessage.get().get(0).contains("Why have the developers of this game not said anything regarding the fraudulent chest rates")) {
        event.getChannel().sendMessage(
            "I was just told in #deutsch by the admin of the facebook group, that my comment \"Why have the developers of this game still not said anything regarding the fraudulent chest rates?\" will not be permitted. The official facebook group has become an echo chamber of exclusively positive feedback for the game.\n"
            + "\n"
            + "I also received a screenshot of the following response to @Biase87 (GH71) trying to post something in regards to the key rate on facebook."
            ).queue();
      }
    }
  }

  
  
}
