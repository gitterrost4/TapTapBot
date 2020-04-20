package listeners;

import java.util.Timer;
import java.util.TimerTask;

import config.Config;
import net.dv8tion.jda.api.JDA;

public class ServerStatsListener extends AbstractListener {

  public ServerStatsListener(JDA jda) {
    super(jda);
    Timer t = new Timer();
    t.scheduleAtFixedRate(new StatsUpdater(), 10000, 10000);
  }

  private class StatsUpdater extends TimerTask {

    @Override
    public void run() {
      guild().getVoiceChannelById(Config.get("serverstats.userCountChannelId")).getManager()
          .setName("Members: " + guild().getMembersWithRoles(guild().getRolesByName("Member", false)).size()).queue();
      guild().getVoiceChannelById(Config.get("serverstats.androidCountChannelId")).getManager()
          .setName("Android: " + guild().getMembersWithRoles(guild().getRolesByName("Android", false)).size()).queue();
      guild().getVoiceChannelById(Config.get("serverstats.iosCountChannelId")).getManager()
          .setName("iOS: " + guild().getMembersWithRoles(guild().getRolesByName("iOS", false)).size()).queue();
      guild().getVoiceChannelById(Config.get("serverstats.welcomeCountChannelId")).getManager()
          .setName("Welcome: " + guild().getMembersWithRoles(guild().getRolesByName("Welcome", false)).size()).queue();
    }
  }

}
