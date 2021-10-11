package de.gitterrost4.taptapbot.config.containers.modules;

import java.time.Duration;
import java.util.List;

import de.gitterrost4.botlib.config.containers.modules.ModuleConfig;

public class HugConfig extends ModuleConfig {
  private String hugChannelId;
  private Duration timeout;
  private List<String> quotes;
  private List<String> hugEmotes;
  public String getHugChannelId() {
    return hugChannelId;
  }
  public List<String> getQuotes() {
    return quotes;
  }
  public List<String> getHugEmotes() {
    return hugEmotes;
  }
  public Duration getTimeout() {
    return timeout;
  }
  
}
