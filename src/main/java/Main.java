import javax.security.auth.login.LoginException;

import config.Config;
import listeners.BotJoinListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * main method
 */
public class Main extends ListenerAdapter {

  public static void main(String[] args) throws LoginException, InterruptedException {
    JDA jda = new JDABuilder(Config.getToken()).build().awaitReady();
    jda.addEventListener(new BotJoinListener(jda)); // Listener for new servers
    Config.getConfig().getServers().stream().forEach(config -> {
      config.addServerModules(jda);
    });

  }
}

// end of file
