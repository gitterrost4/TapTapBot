package de.gitterrost4.taptapbot.config.containers.modules;

import de.gitterrost4.botlib.config.containers.modules.CommandModuleConfig;

public class RoleConfig extends CommandModuleConfig {
  private String reactionChannelId;

  public String getReactionChannelId() {
    return reactionChannelId;
  }

}
