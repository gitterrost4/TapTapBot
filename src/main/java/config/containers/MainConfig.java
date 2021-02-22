package config.containers;

import java.io.IOException;

import config.Config;

public class MainConfig extends de.gitterrost4.botlib.config.containers.MainConfig<ServerConfigImpl>{
  private ServerConfigImpl defaultConfig;

  @Override
  public ServerConfigImpl getDefaultConfig() {
    try {
      ServerConfigImpl config = Config.objectMapper().treeToValue(Config.objectMapper().valueToTree(defaultConfig),
          ServerConfigImpl.class); // this is a dirty, dirty hack... deep-copying the whole object by
                               // serializing/deserializing it
      return config;
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

}
