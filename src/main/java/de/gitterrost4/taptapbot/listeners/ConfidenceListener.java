package de.gitterrost4.taptapbot.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import de.gitterrost4.botlib.containers.CommandMessage;
import de.gitterrost4.botlib.listeners.AbstractMessageListener;
import de.gitterrost4.taptapbot.config.containers.ServerConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ConfidenceListener extends AbstractMessageListener<ServerConfig> {

  public static Map<Double,Double> zValues = new HashMap<>();
  static {
    zValues.put(0.8, 1.282);
    zValues.put(0.9, 1.645);
    zValues.put(0.99, 2.576);
    zValues.put(0.995, 2.807);
    zValues.put(0.999, 3.291);
    zValues.put(0.9999,3.891);
  }
  
  public ConfidenceListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getPullStatsConfig(), "confidence");
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage message) {
    int totalPulls = Integer.valueOf(message.getArgOrThrow(0));
    int fiveStarPulls = Integer.valueOf(message.getArgOrThrow(1));
    event.getMessage().delete().queue();
    String result = getResult(totalPulls,fiveStarPulls);
    event.getChannel().sendMessage(result).queue();
  }
  
  public String getResult(Integer totalPulls, Integer fiveStarPulls) {
    double N = totalPulls;
    double F = fiveStarPulls;
    double mean = F/N;
    StringBuilder sb = new StringBuilder();
    sb.append("***Confidence intervals***\n");
    sb.append("Gold Keys pulled: "+totalPulls+"\n");
    sb.append("5\\*s pulled: "+fiveStarPulls+"\n");
    sb.append("\n");
    
    sb.append(zValues.entrySet().stream().sorted((e1,e2)->e1.getKey().compareTo(e2.getKey())).map(e->{
      double z = e.getValue();
      double deviation = z*(Math.sqrt(F*(1-mean)*(1-mean)+(N-F)*mean*mean)/N);
      double minRate = mean-deviation;
      double maxRate = mean+deviation;
      return String.format("With %2.2f%% confidence we can say that the rate is between %1.2f%% and %1.2f%%", e.getKey()*100, minRate*100, maxRate*100);
    }).collect(Collectors.joining("\n")));
    
    sb.append("\n\nTo repeat this command do "+commandString(totalPulls+" "+fiveStarPulls));
    return sb.toString();
  }


  @Override
  protected String shortInfoInternal() {
    return "Do some analysis about gold key pulls";
  }

  @Override
  protected String usageInternal() {
    return commandString("<NUMBER_OF_PULLS> <NUMBER_OF_FIVE_STARS>")+"\n";
  }

  @Override
  protected String descriptionInternal() {
    return "Do some analysis for pulling NUMBER_OF_PULLS gold key pulls and getting NUMBER_OF_FIVE_STARS 5* heroes.";
  }

  @Override
  protected String examplesInternal() {
    return commandString("5280 225")+"\n" + "Display analysis for 5280 pulls and 225 five-star-heroes.";
  }  
  
}
