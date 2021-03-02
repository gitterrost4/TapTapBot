package de.gitterrost4.taptapbot.config.containers.modules;

import de.gitterrost4.botlib.config.containers.modules.CommandModuleConfig;

public class SuggestionsConfig extends CommandModuleConfig {
  private String bestChannelId;
  private String channelId;
  private String controversialChannelId;
  private String doneChannelId;
  private Integer maxSuggestionsPerUser;
  private Integer maxSuggestionsTimeoutSeconds;
  private String topChannelId;
  private Integer topCount;
  private Integer topUpdateIntervalSeconds;
  private String worstChannelId;
  
  public String getBestChannelId() {
    return bestChannelId;
  }

  public String getChannelId() {
    return channelId;
  }

  public String getControversialChannelId() {
    return controversialChannelId;
  }

  public String getDoneChannelId() {
    return doneChannelId;
  }

  public Integer getMaxSuggestionsPerUser() {
    return maxSuggestionsPerUser;
  }

  public Integer getMaxSuggestionsTimeoutSeconds() {
    return maxSuggestionsTimeoutSeconds;
  }

  public String getTopChannelId() {
    return topChannelId;
  }

  public Integer getTopCount() {
    return topCount;
  }

  public Integer getTopUpdateIntervalSeconds() {
    return topUpdateIntervalSeconds;
  }

  public String getWorstChannelId() {
    return worstChannelId;
  }

  @Override
  public String toString() {
    return "SuggestionsConfig [bestChannelId=" + bestChannelId + ", channelId=" + channelId
        + ", controversialChannelId=" + controversialChannelId + ", doneChannelId=" + doneChannelId
        + ", maxSuggestionsPerUser=" + maxSuggestionsPerUser + ", maxSuggestionsTimeoutSeconds="
        + maxSuggestionsTimeoutSeconds + ", topChannelId=" + topChannelId + ", topCount=" + topCount
        + ", topUpdateIntervalSeconds=" + topUpdateIntervalSeconds + ", worstChannelId=" + worstChannelId + "]";
  }
}
