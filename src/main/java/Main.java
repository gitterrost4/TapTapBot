import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

// $Id $
// (C) cantamen/Paul Kramer 2019

/**
 * TODO documentation
 *
 * @author (C) cantamen/Paul Kramer 2019
 * @version $Id $
 */
public class Main extends ListenerAdapter {

  public static void main(String[] args) throws LoginException, FileNotFoundException, IOException {
    try (InputStream input=new FileInputStream("cnf/config.properties")) {
      Properties prop=new Properties();
      prop.load(input);
      JDA jda=new JDABuilder(prop.getProperty("bot.token")).build();
      jda.addEventListener(new EventListener(jda));
    }
  }
}

// end of file
