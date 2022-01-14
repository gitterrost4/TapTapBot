package de.gitterrost4.taptapbot.listeners;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.gitterrost4.botlib.listeners.AbstractListener;
import de.gitterrost4.taptapbot.config.containers.ServerConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;

public class MessagePosterListener extends AbstractListener<ServerConfig> {

  public String currentMessageId;
  
  public MessagePosterListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getMessagePosterConfig());
    currentMessageId = config.getMessagePosterConfig().messageId;
  }

  @Override
  public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
    super.onGuildMessageDelete(event);
    if(event.getMessageId().equals(currentMessageId)) {
      try (InputStream input = new FileInputStream("message.png")) {
        event.getChannel().sendMessage(
            "I was just told in #deutsch by the admin of the facebook group, that my comment \"Why have the developers of this game still not said anything regarding the fraudulent chest rates?\" will not be permitted. The official facebook group has become an echo chamber of exclusively positive feedback for the game.\n"
            + "\n"
            + "I also received a screenshot of the following response to @Biase87 (GH71) trying to post something in regards to the key rate on facebook."
            ).queue(m->currentMessageId = m.getId());
      } catch (IOException e) {
        event.getChannel().sendMessage(
            "I was just told in #deutsch by the admin of the facebook group, that my comment \"Why have the developers of this game still not said anything regarding the fraudulent chest rates?\" will not be permitted. The official facebook group has become an echo chamber of exclusively positive feedback for the game.\n"
            + "\n"
            + "I also received a screenshot of the following response to @Biase87 (GH71) trying to post something in regards to the key rate on facebook:\n"
            + "https://i.ibb.co/5WGzF8s/20220114123449.png"
            ).queue(m->currentMessageId = m.getId());
      } 
    }
  }

  
  
}
