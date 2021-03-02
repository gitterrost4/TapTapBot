package de.gitterrost4.taptapbot.config.containers.modules;

import de.gitterrost4.botlib.config.containers.modules.ModuleConfig;

public class WelcomeConfig extends ModuleConfig {
  private String androidRoleId;
  private String channelId;
  private String iosRoleId;
  private String memberRoleId;
  private String welcomeRoleId;
  
  public String getAndroidRoleId() {
    return androidRoleId;
  }

  public String getChannelId() {
    return channelId;
  }

  public String getIosRoleId() {
    return iosRoleId;
  }

  public String getMemberRoleId() {
    return memberRoleId;
  }

  public String getWelcomeRoleId() {
    return welcomeRoleId;
  }

}
