import javax.security.auth.login.LoginException;

import config.Config;
import listeners.AutoRespondListener;
import listeners.CalculateListener;
import listeners.GiftCodeListener;
import listeners.MirrorListener;
import listeners.ModlogListener;
import listeners.RoleCountListener;
import listeners.RulesListener;
import listeners.ServerStatsListener;
import listeners.SuggestionsListener;
import listeners.WatchListListener;
import listeners.WelcomeListener;
import listeners.modtools.BanListener;
import listeners.modtools.MuteListener;
import listeners.modtools.PurgeListener;
import listeners.modtools.SettingsListener;
import listeners.modtools.UnmuteListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * main method
 */
public class Main extends ListenerAdapter {

  public static void main(String[] args) throws LoginException {
    JDA jda = new JDABuilder(Config.get("bot.token")).build();
    jda.addEventListener(new SettingsListener(jda));
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
    if (Config.getBool("module.mirror")) {
      jda.addEventListener(new MirrorListener(jda));
    }
    if (Config.getBool("module.modlog")) {
      jda.addEventListener(new ModlogListener(jda));
    }
    if (Config.getBool("module.mute")) {
      jda.addEventListener(new MuteListener(jda));
      jda.addEventListener(new UnmuteListener(jda));
    }
    if (Config.getBool("module.ban")) {
      jda.addEventListener(new BanListener(jda));
    }
    if (Config.getBool("module.serverstats")) {
      jda.addEventListener(new ServerStatsListener(jda));
    }
    if (Config.getBool("module.giftcodes")) {
      jda.addEventListener(new GiftCodeListener(jda));
    }
    if (Config.getBool("module.purge")) {
      jda.addEventListener(new PurgeListener(jda));
    }
    if (Config.getBool("module.autorespond")) {
      jda.addEventListener(new AutoRespondListener(jda));
    }
    if (Config.getBool("module.rolecount")) {
      jda.addEventListener(new RoleCountListener(jda));
    }

//    Catcher.wrap(() -> Thread.sleep(5000));
//    EmbedBuilder builder = new EmbedBuilder();
//    AbstractListener.setEmbedAuthor(builder, jda.getGuildById(Config.get("bot.serverId")).getSelfMember());
//    builder.setDescription("Message edited in #test");
//    builder.addField("oldtext", "hello", true);
//    builder.addField("newtext", "world", false);
//
//    jda.getGuildById(Config.get("bot.serverId")).getTextChannelById("614030842238337026").sendMessage(builder.build())
//        .queue();
  }
}

// end of file
