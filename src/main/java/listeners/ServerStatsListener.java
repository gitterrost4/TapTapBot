package listeners;

import java.util.Timer;
import java.util.TimerTask;

import config.containers.ServerConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public class ServerStatsListener extends AbstractListener {

  public ServerStatsListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda,guild,config,config.getServerStatsConfig());
    Timer t = new Timer();
    t.scheduleAtFixedRate(new StatsUpdater(), 10, 900000);
  }

  private class StatsUpdater extends TimerTask {

    @Override
    public void run() {
      guild().getVoiceChannelById(config.getServerStatsConfig().getUserCountChannelId()).getManager()
          .setName("Members: " + guild().getMembersWithRoles(guild().getRolesByName("Member", false)).size()).queue();
      guild().getVoiceChannelById(config.getServerStatsConfig().getAndroidCountChannelId()).getManager()
          .setName("Android: " + guild().getMembersWithRoles(guild().getRolesByName("Android", false)).size()).queue();
      guild().getVoiceChannelById(config.getServerStatsConfig().getIosCountChannelId()).getManager()
          .setName("iOS: " + guild().getMembersWithRoles(guild().getRolesByName("iOS", false)).size()).queue();
      guild().getVoiceChannelById(config.getServerStatsConfig().getWelcomeCountChannelId()).getManager()
          .setName("Welcome: " + guild().getMembersWithRoles(guild().getRolesByName("Welcome", false)).size()).queue();
    }
  }

}
