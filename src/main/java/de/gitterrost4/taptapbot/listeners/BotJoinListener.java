package de.gitterrost4.taptapbot.listeners;

import de.gitterrost4.taptapbot.config.Config;
import de.gitterrost4.taptapbot.config.containers.MainConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class BotJoinListener extends ListenerAdapter {

  private final JDA jda;

  public BotJoinListener(JDA jda) {
    super();
    this.jda = jda;
  }

  @Override
  public void onGuildJoin(GuildJoinEvent event) {
    super.onGuildJoin(event);
    Guild guild = event.getGuild();
    MainConfig config = Config.getConfig();
    config.addDefaultServerConfigIfAbsent(guild.getId(), guild.getName(), jda, ()->Config.saveConfig());
  }

}
