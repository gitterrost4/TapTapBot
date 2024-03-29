package de.gitterrost4.taptapbot;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gitterrost4.taptapbot.config.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

/**
 * main method
 */
public class Main extends ListenerAdapter {

  public static void main(String[] args) throws LoginException, InterruptedException {
    Logger logger = LoggerFactory.getLogger(Main.class);
    logger.warn("--------------Starting TapTapBot--------------\n");
    JDA jda = JDABuilder.createDefault(Config.getToken()).setActivity(Activity.playing("Use tap.help for info.")).setMemberCachePolicy(MemberCachePolicy.ALL).setChunkingFilter(ChunkingFilter.ALL).enableIntents(GatewayIntent.GUILD_MEMBERS).build().awaitReady();
    Config.addListeners(jda);
  }
}

// end of file
