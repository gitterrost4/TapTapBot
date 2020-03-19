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
      int memberCount = guild().getMembersWithRoles(guild().getRolesByName("Member", true)).size();
      guild().getVoiceChannelById(Config.get("serverstats.userCountChannelId")).getManager()
          .setName("Members: " + memberCount).queue();
    }
  }

}
