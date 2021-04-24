package de.gitterrost4.taptapbot.config.containers;

import java.util.Optional;

import de.gitterrost4.botlib.config.containers.modules.ModuleConfig;
import de.gitterrost4.taptapbot.config.containers.modules.CalculateConfig;
import de.gitterrost4.taptapbot.config.containers.modules.GiftCodeConfig;
import de.gitterrost4.taptapbot.config.containers.modules.HeroConfig;
import de.gitterrost4.taptapbot.config.containers.modules.MuteConfig;
import de.gitterrost4.taptapbot.config.containers.modules.PurgeConfig;
import de.gitterrost4.taptapbot.config.containers.modules.RoleConfig;
import de.gitterrost4.taptapbot.config.containers.modules.RulesConfig;
import de.gitterrost4.taptapbot.config.containers.modules.ServerStatsConfig;
import de.gitterrost4.taptapbot.config.containers.modules.SuggestionsConfig;
import de.gitterrost4.taptapbot.config.containers.modules.WatchlistConfig;
import de.gitterrost4.taptapbot.config.containers.modules.WelcomeConfig;
import de.gitterrost4.taptapbot.listeners.CalculateListener;
import de.gitterrost4.taptapbot.listeners.GiftCodeListener;
import de.gitterrost4.taptapbot.listeners.HeroListener;
import de.gitterrost4.taptapbot.listeners.HeroStoryListener;
import de.gitterrost4.taptapbot.listeners.RulesListener;
import de.gitterrost4.taptapbot.listeners.ServerStatsListener;
import de.gitterrost4.taptapbot.listeners.SuggestionsListener;
import de.gitterrost4.taptapbot.listeners.SuggestionsStatsListener;
import de.gitterrost4.taptapbot.listeners.WatchListListener;
import de.gitterrost4.taptapbot.listeners.WelcomeListener;
import de.gitterrost4.taptapbot.listeners.modtools.MuteListener;
import de.gitterrost4.taptapbot.listeners.modtools.PurgeListener;
import de.gitterrost4.taptapbot.listeners.modtools.RoleListener;
import de.gitterrost4.taptapbot.listeners.modtools.UnmuteListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public class ServerConfig extends de.gitterrost4.botlib.config.containers.ServerConfig {
  private CalculateConfig calculateConfig;
  private GiftCodeConfig giftCodeConfig;
  private HeroConfig heroConfig;
  private MuteConfig muteConfig;
  private PurgeConfig purgeConfig;
  private RoleConfig roleConfig;
  private RulesConfig rulesConfig;
  private ServerStatsConfig serverStatsConfig;
  private SuggestionsConfig suggestionsConfig;
  private WatchlistConfig watchlistConfig;
  private WelcomeConfig welcomeConfig;

  @Override
  public String toString() {
    return "ServerConfigImpl [calculateConfig="
        + calculateConfig + ", giftCodeConfig=" + giftCodeConfig + ", heroConfig=" + heroConfig 
        + ", muteConfig=" + muteConfig + ", purgeConfig="
        + purgeConfig + ", roleConfig=" + roleConfig + ", rulesConfig=" + rulesConfig + ", serverStatsConfig=" + serverStatsConfig
        + ", suggestionsConfig=" + suggestionsConfig + ", watchlistConfig=" + watchlistConfig + ", welcomeConfig="
        + welcomeConfig + ", toString()="
        + super.toString() + "]";
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

  public MuteConfig getMuteConfig() {
    return muteConfig;
  }

  public PurgeConfig getPurgeConfig() {
    return purgeConfig;
  }

  public RoleConfig getRoleConfig() {
    return roleConfig;
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
    
  @Override
  protected void addServerModules(JDA jda, Guild guild, de.gitterrost4.botlib.listeners.ListenerManager manager) {
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
    if (Optional.ofNullable(getMuteConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new MuteListener(jda, guild, this));
      manager.addEventListener(new UnmuteListener(jda, guild, this));
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
    if (Optional.ofNullable(getRoleConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new RoleListener(jda, guild, this));
    }
    if (Optional.ofNullable(getHeroConfig()).map(ModuleConfig::isEnabled).orElse(false)) {
      manager.addEventListener(new HeroListener(jda, guild, this));
      manager.addEventListener(new HeroStoryListener(jda, guild, this));
    }
  }

}
