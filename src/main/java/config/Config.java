// $Id $
// (C) cantamen/Paul Kramer 2019
package config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import config.containers.MainConfig;

/**
 * global config
 */
public class Config {
  private static Config instance = new Config();
  private final String token;
  public MainConfig config;

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
      ObjectMapper mapper = objectMapper();
      config = mapper.readValue(input, MainConfig.class);
    } catch (IOException e) {
      throw new IllegalStateException("couldn't read config", e);
    }
  }

  private void saveConfigI() {
    try (OutputStream output = new FileOutputStream("config.json")) {
      ObjectMapper mapper = objectMapper();
      mapper.writeValue(output, config);
    } catch (IOException e) {
      throw new IllegalStateException("couldn't read config", e);
    }
  }

  public static void saveConfig() {
    instance.saveConfigI();
  }

  public static ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(Include.NON_NULL);
    mapper.registerModule(new Jdk8Module());
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    return mapper;
  }

  public static String getToken() {
    return instance.token;
  }

  public static MainConfig getConfig() {
    return instance.config;
  }

}

// end of file
