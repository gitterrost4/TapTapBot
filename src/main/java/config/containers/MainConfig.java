package config.containers;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.Config;
import net.dv8tion.jda.api.JDA;

public class MainConfig {
  private List<ServerConfig> servers;
  private ServerConfig defaultConfig;

  private static final Logger logger = LoggerFactory.getLogger(MainConfig.class);

  public List<ServerConfig> getServers() {
    return servers;
  }

  public ServerConfig getDefaultConfig() {
    return defaultConfig;
  }

  public void addDefaultServerConfigIfAbsent(String serverId, String serverName, JDA jda) {
    try {
      if (!servers.stream().anyMatch(serverConfig -> serverConfig.getServerId().equals(serverId))) {
        // we don't have a config for this server. We need this check because when
        // discord is down, this event may be triggered for servers we already joined.
        logger.info("Adding Guild {}({})", serverId, serverName);
        ServerConfig config = Config.objectMapper().treeToValue(Config.objectMapper().valueToTree(defaultConfig),
            ServerConfig.class); // this is a dirty, dirty hack... deep-copying the whole object by
                                 // serializing/deserializing it
        config.serverId = serverId;
        config.name = serverName;
        config.databaseFileName = serverId + ".db";
        servers.add(config);
        config.addServerModules(jda);
        Config.saveConfig();

      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

}
