package de.gitterrost4.taptapbot.listeners;

import java.util.Optional;

import de.gitterrost4.botlib.listeners.AbstractListener;
import de.gitterrost4.taptapbot.config.containers.ServerConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HugListener extends AbstractListener<ServerConfig>{

  public HugListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getHugConfig());
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event) {
    super.messageReceived(event);
    if(event.getChannel().getId().equals(config.getHugConfig().getHugChannelId())) {
      String message = config.getHugConfig().getQuotes().stream().skip((int) (config.getHugConfig().getQuotes().size() * Math.random())).findFirst().orElseThrow(()->new IllegalStateException("no quotes configured"));
      Optional<String> emote = config.getHugConfig().getHugEmotes().stream().skip((int) (config.getHugConfig().getHugEmotes().size() * Math.random())).findFirst();
      event.getMessage().reply(emote.map(s->s+s+s+"\n").orElse("")+message).queue();
    }
  }
  
  
  

}
