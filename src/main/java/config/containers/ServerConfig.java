package config.containers;

import config.containers.modules.AutoRespondConfig;
import config.containers.modules.BanConfig;
import config.containers.modules.CalculateConfig;
import config.containers.modules.GiftCodeConfig;
import config.containers.modules.HeroConfig;
import config.containers.modules.MirrorConfig;
import config.containers.modules.ModlogConfig;
import config.containers.modules.MuteConfig;
import config.containers.modules.PurgeConfig;
import config.containers.modules.RoleConfig;
import config.containers.modules.RoleCountConfig;
import config.containers.modules.RulesConfig;
import config.containers.modules.ServerStatsConfig;
import config.containers.modules.SuggestionsConfig;
import config.containers.modules.WatchlistConfig;
import config.containers.modules.WelcomeConfig;

public class ServerConfig {
  private String name;
  private String serverId;
  private String botPrefix;
  private String databaseFileName;
  private AutoRespondConfig autoRespondConfig;
  private BanConfig banConfig;
  private CalculateConfig calculateConfig;
  private GiftCodeConfig giftCodeConfig;
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
    return "ServerConfig [name=" + name + ", serverId=" + serverId + ", botPrefix=" + botPrefix+ ", databaseFileName=" + databaseFileName + ", autoRespondConfig="
        + autoRespondConfig + ", banConfig=" + banConfig + ", calculateConfig=" + calculateConfig + ", giftCodeConfig="
        + giftCodeConfig + ", heroConfig=" + heroConfig + ", mirrorConfig=" + mirrorConfig + ", modlogConfig="
        + modlogConfig + ", muteConfig=" + muteConfig + ", purgeConfig=" + purgeConfig + ", roleConfig=" + roleConfig
        + ", roleCountConfig=" + roleCountConfig + ", rulesConfig=" + rulesConfig + ", serverStatsConfig="
        + serverStatsConfig + ", suggestionsConfig=" + suggestionsConfig + ", watchlistConfig=" + watchlistConfig
        + ", welcomeConfig=" + welcomeConfig + "]";
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

}
