package config.containers.modules;

import java.util.Optional;

import de.gitterrost4.botlib.config.containers.modules.CommandModuleConfig;

public class HeroConfig extends CommandModuleConfig {
  private String minimumEditRole;
  private String databaseFileName;
  private Boolean editAllowed;

  public String getMinimumEditRole() {
    return minimumEditRole;
  }

  public String getDatabaseFileName() {
    return databaseFileName;
  }

  public Boolean getEditAllowed() {
    return Optional.ofNullable(editAllowed).orElse(false);
  }

}
