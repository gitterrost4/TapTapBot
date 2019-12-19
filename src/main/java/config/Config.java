// $Id $
// (C) cantamen/Paul Kramer 2019
package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** 
 * global config
 *
 */
public class Config {
  private static Config instance = new Config();
  public Properties props;
  public Config(){
    try (InputStream input=new FileInputStream("cnf/config.properties")) {
      Properties prop=new Properties();
      prop.load(input);
      props = prop;
    } catch (IOException e) {
      throw new IllegalStateException("couldn't read config");
    }    
  }
  
  public static String get(String key) {
    return instance.props.getProperty(key);
  }
  
}


// end of file
