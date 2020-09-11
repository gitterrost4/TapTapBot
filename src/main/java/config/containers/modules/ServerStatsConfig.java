package config.containers.modules;

public class ServerStatsConfig extends ModuleConfig {
  private String androidCountChannelId;
  private String iosCountChannelId;
  private String userCountChannelId;
  private String welcomeCountChannelId;
  
  public String getAndroidCountChannelId() {
    return androidCountChannelId;
  }

  public String getIosCountChannelId() {
    return iosCountChannelId;
  }

  public String getUserCountChannelId() {
    return userCountChannelId;
  }

  public String getWelcomeCountChannelId() {
    return welcomeCountChannelId;
  }

}
