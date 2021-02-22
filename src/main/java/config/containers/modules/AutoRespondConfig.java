package config.containers.modules;

import de.gitterrost4.botlib.config.containers.modules.CommandModuleConfig;

public class AutoRespondConfig extends CommandModuleConfig{
  private String minRoleId;

  public String getMinRoleId() {
    return minRoleId;
  }

}
