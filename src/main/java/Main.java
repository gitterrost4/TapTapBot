import javax.security.auth.login.LoginException;

import config.Config;
import listeners.CalculateListener;
import listeners.RulesListener;
import listeners.SuggestionsListener;
import listeners.WatchListListener;
import listeners.WelcomeListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

// $Id $
// (C) cantamen/Paul Kramer 2019

/**
 * main method
 */
public class Main extends ListenerAdapter {

  public static void main(String[] args) throws LoginException {
    JDA jda = new JDABuilder(Config.get("bot.token")).build();
    if (Config.getBool("module.suggestions")) {
      jda.addEventListener(new SuggestionsListener(jda));
    }
    if (Config.getBool("module.watchlist")) {
      jda.addEventListener(new WatchListListener(jda));
    }
    if (Config.getBool("module.calculate")) {
      jda.addEventListener(new CalculateListener(jda));
    }
    if (Config.getBool("module.welcome")) {
      jda.addEventListener(new WelcomeListener(jda));
    }
    if (Config.getBool("module.rules")) {
      jda.addEventListener(new RulesListener(jda));
    }
  }
}

// end of file
