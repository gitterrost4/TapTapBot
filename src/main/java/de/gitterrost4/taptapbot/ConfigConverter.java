package de.gitterrost4.taptapbot;
import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.gitterrost4.taptapbot.config.Config;
import de.gitterrost4.taptapbot.config.containers.MainConfig;

public class ConfigConverter {

  public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {
    MainConfig config = Config.getConfig();
    Config.yamlObjectMapper().writeValue(new File("config.yaml"), config);
  }

}
