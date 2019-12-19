import javax.security.auth.login.LoginException;

import config.Config;
import listeners.SuggestionsListener;
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
    JDA jda=new JDABuilder(Config.get("bot.token")).build();
    jda.addEventListener(new SuggestionsListener(jda));
  }
}

// end of file
