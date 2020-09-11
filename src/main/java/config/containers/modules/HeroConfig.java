package config.containers.modules;

import java.util.Optional;

public class HeroConfig extends ModuleConfig {
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
