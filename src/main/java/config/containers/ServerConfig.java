package config.containers;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.containers.modules.AutoRespondConfig;
import config.containers.modules.BanConfig;
import config.containers.modules.CalculateConfig;
import config.containers.modules.GiftCodeConfig;
import config.containers.modules.HelpConfig;
import config.containers.modules.HeroConfig;
import config.containers.modules.MirrorConfig;
import config.containers.modules.ModlogConfig;
import config.containers.modules.ModuleConfig;
import config.containers.modules.MuteConfig;
import config.containers.modules.PurgeConfig;
import config.containers.modules.RoleConfig;
import config.containers.modules.RoleCountConfig;
import config.containers.modules.RulesConfig;
import config.containers.modules.ServerStatsConfig;
import config.containers.modules.SuggestionsConfig;
import config.containers.modules.WatchlistConfig;
import config.containers.modules.WelcomeConfig;
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
import net.dv8tion.jda.api.entities.Guild;

public class ServerConfig {
  private static final Logger logger = LoggerFactory.getLogger(ServerConfig.class);

  String name;
  String serverId;
  private String botPrefix;
  String databaseFileName;
  private AutoRespondConfig autoRespondConfig;
  private BanConfig banConfig;
  private CalculateConfig calculateConfig;
  private GiftCodeConfig giftCodeConfig;
  private HelpConfig helpConfig;
  private HeroConfig heroConfig;
  private MirrorConfig mirrorConfig;
  private ModlogConfig modlogConfig;
  private MuteConfig muteConfig;
  private PurgeConfig purgeConfig;
  private RoleConfig roleConfig;
  private RoleCountConfig roleCountConfig;
  private RulesConfig rulesConfig;
  private ServerStatsConfig serverStatsConfig;
  private SuggestionsConfig suggestionsConfig;
  private WatchlistConfig watchlistConfig;
  private WelcomeConfig welcomeConfig;

  @Override
  public String toString() {
    return "ServerConfig [name=" + name + ", serverId=" + serverId + ", botPrefix=" + botPrefix + ", databaseFileName="
        + databaseFileName + ", autoRespondConfig=" + autoRespondConfig + ", banConfig=" + banConfig
        + ", calculateConfig=" + calculateConfig + ", giftCodeConfig=" + giftCodeConfig + ", heroConfig=" + heroConfig
        + ", mirrorConfig=" + mirrorConfig + ", modlogConfig=" + modlogConfig + ", muteConfig=" + muteConfig
        + ", purgeConfig=" + purgeConfig + ", roleConfig=" + roleConfig + ", roleCountConfig=" + roleCountConfig
        + ", rulesConfig=" + rulesConfig + ", serverStatsConfig=" + serverStatsConfig + ", suggestionsConfig="
        + suggestionsConfig + ", watchlistConfig=" + watchlistConfig + ", welcomeConfig=" + welcomeConfig + "]";
  }

  public String getName() {
    return name;
  }

  public String getServerId() {
    return serverId;
  }

  public String getBotPrefix() {
    return botPrefix;
  }

  public String getDatabaseFileName() {
    return databaseFileName;
  }

  public AutoRespondConfig getAutoRespondConfig() {
    return autoRespondConfig;
  }

  public BanConfig getBanConfig() {
    return banConfig;
  }

  public CalculateConfig getCalculateConfig() {
    return calculateConfig;
  }

  public GiftCodeConfig getGiftCodeConfig() {
    return giftCodeConfig;
  }

  public HeroConfig getHeroConfig() {
    return heroConfig;
  }

  public MirrorConfig getMirrorConfig() {
    return mirrorConfig;
  }

  public ModlogConfig getModlogConfig() {
    return modlogConfig;
  }

  public MuteConfig getMuteConfig() {
    return muteConfig;
  }

  public PurgeConfig getPurgeConfig() {
    return purgeConfig;
  }

  public RoleConfig getRoleConfig() {
    return roleConfig;
  }

  public HelpConfig getHelpConfig() {
    return helpConfig;
  }

  public RoleCountConfig getRoleCountConfig() {
    return roleCountConfig;
  }

  public RulesConfig getRulesConfig() {
    return rulesConfig;
  }

  public ServerStatsConfig getServerStatsConfig() {
    return serverStatsConfig;
  }

  public SuggestionsConfig getSuggestionsConfig() {
    return suggestionsConfig;
  }

  public WatchlistConfig getWatchlistConfig() {
    return watchlistConfig;
  }

  public WelcomeConfig getWelcomeConfig() {
    return welcomeConfig;
  }

  public void addServerModules(JDA jda) {
    Guild guild = jda.getGuildById(getServerId());
    logger.info("Adding Guild {}({})", guild.getId(), guild.getName());
    ListenerManager manager = new ListenerManager(jda);

    // jda.addEventListener(new SettingsListener(jda,guild,config)); does not work
    // currently TODO: Rewrite that handler
    if (Optional.ofNullable(getSuggestionsConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new SuggestionsListener(jda, guild, this));
      manager.addEventListener(new SuggestionsStatsListener(jda, guild, this));
    }
    if (Optional.ofNullable(getWatchlistConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new WatchListListener(jda, guild, this));
    }
    if (Optional.ofNullable(getCalculateConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new CalculateListener(jda, guild, this));
    }
    if (Optional.ofNullable(getWelcomeConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new WelcomeListener(jda, guild, this));
    }
    if (Optional.ofNullable(getRulesConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new RulesListener(jda, guild, this));
    }
    if (Optional.ofNullable(getMirrorConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new MirrorListener(jda, guild, this));
    }
    if (Optional.ofNullable(getModlogConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new ModlogListener(jda, guild, this));
    }
    if (Optional.ofNullable(getMuteConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new MuteListener(jda, guild, this));
      manager.addEventListener(new UnmuteListener(jda, guild, this));
    }
    if (Optional.ofNullable(getBanConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new BanListener(jda, guild, this));
    }
    if (Optional.ofNullable(getServerStatsConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new ServerStatsListener(jda, guild, this));
    }
    if (Optional.ofNullable(getGiftCodeConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new GiftCodeListener(jda, guild, this));
    }
    if (Optional.ofNullable(getPurgeConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new PurgeListener(jda, guild, this));
    }
    if (Optional.ofNullable(getAutoRespondConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new AutoRespondListener(jda, guild, this));
    }
    if (Optional.ofNullable(getRoleCountConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new RoleCountListener(jda, guild, this));
    }
    if (Optional.ofNullable(getRoleConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new RoleListener(jda, guild, this));
    }
    if (Optional.ofNullable(getHeroConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new HeroListener(jda, guild, this));
    }
    if (Optional.ofNullable(getHelpConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new HelpListener(jda, guild, this, manager));
    }
  }

}
