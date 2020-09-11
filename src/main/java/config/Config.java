// $Id $
// (C) cantamen/Paul Kramer 2019
package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import config.containers.ServerConfig;

/**
 * global config
 */
public class Config {
  private static Config instance = new Config();
  private final String token;
  public List<ServerConfig> config;

  public Config() {
    try (InputStream input = new FileInputStream("token.secret")) {
//    try (InputStream input=this.getClass().getClassLoader().getResourceAsStream("config.properties")) {
      byte[] buf = new byte[59];
      input.read(buf);
      token = new String(buf, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("couldn't read token", e);
    }
    try (InputStream input = new FileInputStream("config.json")) {
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(new Jdk8Module());
      config = mapper.readValue(input, new TypeReference<List<ServerConfig>>() {});
    } catch (IOException e) {
      throw new IllegalStateException("couldn't read config", e);
    }
  }

  public static String getToken() {
    return instance.token;
  }
  
  public static Collection<ServerConfig> getConfigs() {
    return instance.config;
  }
  
}

// end of file
