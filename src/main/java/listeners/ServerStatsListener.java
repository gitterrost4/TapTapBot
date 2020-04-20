package listeners;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import config.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;

public class ServerStatsListener extends AbstractListener {

  public ServerStatsListener(JDA jda) {
    super(jda);
    Timer t = new Timer();
    t.scheduleAtFixedRate(new StatsUpdater(), 10000, 10000);
  }

  private class StatsUpdater extends TimerTask {

    @Override
    public void run() {
      List<Role> rolesByName = guild().getRolesByName("Member", false);
      System.err.println(rolesByName);
      List<Role> welcomeRolesByName = guild().getRolesByName("Welcome", false);
      System.err.println(welcomeRolesByName);
      int memberCount = guild().getMembersWithRoles(rolesByName).size();
      guild().getVoiceChannelById(Config.get("serverstats.userCountChannelId")).getManager()
          .setName("Members: " + memberCount).queue();
    }
  }

}
