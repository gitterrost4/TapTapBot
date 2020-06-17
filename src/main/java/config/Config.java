// $Id $
// (C) cantamen/Paul Kramer 2019
package config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * global config
 */
public class Config {
  private static Config instance = new Config();
  public Properties props;

  public Config() {
    try (InputStream input = new FileInputStream("config.properties")) {
//    try (InputStream input=this.getClass().getClassLoader().getResourceAsStream("config.properties")) {
      Properties prop = new Properties();
      prop.load(input);
      props = prop;
    } catch (IOException e) {
      throw new IllegalStateException("couldn't read config", e);
    }
  }

  public static String get(String key) {
    return instance.props.getProperty(key);
  }

  public static Boolean getBool(String key) {
    return get(key) != null && get(key).equals("true");
  }

  public static Integer getInt(String key) {
    return Integer.parseInt(get(key));
  }

  public static void writeValue(String key, String value) {
    instance.props.setProperty(key, value);
    try (OutputStream output = new FileOutputStream("config.properties")) {
      instance.props.store(output, "Automatically written by bot");
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static Map<String, String> getAll() {
    return instance.props.stringPropertyNames().stream().map(x -> new SimpleEntry<>(x, instance.props.getProperty(x)))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

}

// end of file
