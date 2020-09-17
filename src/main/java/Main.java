import java.util.Optional;

import javax.security.auth.login.LoginException;

import config.Config;
import config.containers.modules.ModuleConfig;
import listeners.AutoRespondListener;
import listeners.CalculateListener;
import listeners.GiftCodeListener;
import listeners.HelpListener;
import listeners.HeroListener;
import listeners.ListenerManager;
import listeners.MirrorListener;
import listeners.ModlogListener;
import listeners.RoleCountListener;
import listeners.RulesListener;
import listeners.ServerStatsListener;
import listeners.SuggestionsListener;
import listeners.SuggestionsStatsListener;
import listeners.WatchListListener;
import listeners.WelcomeListener;
import listeners.modtools.BanListener;
import listeners.modtools.MuteListener;
import listeners.modtools.PurgeListener;
import listeners.modtools.RoleListener;
import listeners.modtools.UnmuteListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * main method
 */
public class Main extends ListenerAdapter {

  public static void main(String[] args) throws LoginException, InterruptedException {
    JDA jda = new JDABuilder(Config.getToken()).build().awaitReady();
    Config.getConfigs().stream().forEach(config->{
      Guild guild = jda.getGuildById(config.getServerId());
      System.err.println(config.getServerId()+" "+guild);
      ListenerManager manager = new ListenerManager(jda);
      
      //jda.addEventListener(new SettingsListener(jda,guild,config)); does not work currently TODO: Rewrite that handler
      if (Optional.ofNullable(config.getSuggestionsConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
        manager.addEventListener(new SuggestionsListener(jda,guild,config));
        manager.addEventListener(new SuggestionsStatsListener(jda,guild,config));
      }
      if (Optional.ofNullable(config.getWatchlistConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
        manager.addEventListener(new WatchListListener(jda,guild,config));
      }
      if (Optional.ofNullable(config.getCalculateConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
        manager.addEventListener(new CalculateListener(jda,guild,config));
      }
      if (Optional.ofNullable(config.getWelcomeConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
        manager.addEventListener(new WelcomeListener(jda,guild,config));
      }
      if (Optional.ofNullable(config.getRulesConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
        manager.addEventListener(new RulesListener(jda,guild,config));
      }
      if (Optional.ofNullable(config.getMirrorConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
        manager.addEventListener(new MirrorListener(jda,guild,config));
      }
      if (Optional.ofNullable(config.getModlogConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
        manager.addEventListener(new ModlogListener(jda,guild,config));
      }
      if (Optional.ofNullable(config.getMuteConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
        manager.addEventListener(new MuteListener(jda,guild,config));
        manager.addEventListener(new UnmuteListener(jda,guild,config));
      }
      if (Optional.ofNullable(config.getBanConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
        manager.addEventListener(new BanListener(jda,guild,config));
      }
      if (Optional.ofNullable(config.getServerStatsConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
        manager.addEventListener(new ServerStatsListener(jda,guild,config));
      }
      if (Optional.ofNullable(config.getGiftCodeConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
        manager.addEventListener(new GiftCodeListener(jda,guild,config));
      }
      if (Optional.ofNullable(config.getPurgeConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
        manager.addEventListener(new PurgeListener(jda,guild,config));
      }
      if (Optional.ofNullable(config.getAutoRespondConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
        manager.addEventListener(new AutoRespondListener(jda,guild,config));
      }
      if (Optional.ofNullable(config.getRoleCountConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
        manager.addEventListener(new RoleCountListener(jda,guild,config));
      }
      if (Optional.ofNullable(config.getRoleConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
        manager.addEventListener(new RoleListener(jda,guild,config));
      }
      if (Optional.ofNullable(config.getHeroConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
        manager.addEventListener(new HeroListener(jda,guild,config));
      }
      if (Optional.ofNullable(config.getHelpConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
        manager.addEventListener(new HelpListener(jda,guild,config,manager));
      }
    });

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
