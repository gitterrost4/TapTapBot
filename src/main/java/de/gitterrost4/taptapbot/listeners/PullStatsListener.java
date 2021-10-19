package de.gitterrost4.taptapbot.listeners;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import de.gitterrost4.botlib.containers.CommandMessage;
import de.gitterrost4.botlib.listeners.AbstractMessageListener;
import de.gitterrost4.taptapbot.config.containers.ServerConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PullStatsListener extends AbstractMessageListener<ServerConfig> {

  public PullStatsListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getPullStatsConfig(), "pullstats");
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage message) {
    // TODO Auto-generated method stub
    int totalPulls = Integer.valueOf(message.getArgOrThrow(0));
    int fiveStarPulls = Integer.valueOf(message.getArgOrThrow(1));
    double startRate = message.getArg(2).map(Double::valueOf).orElse(0.031);
    double endRate = message.getArg(3).map(Double::valueOf).orElse(0.061);
    double step = message.getArg(4).map(Double::valueOf).orElse(0.001);
    int instancesPerRate = message.getArg(5).map(Integer::valueOf).orElse(3000);
    event.getMessage().delete().queue();
    Message waitMessage = event.getChannel().sendMessage("Calculating...").complete();
    
    String result = getResult(totalPulls,fiveStarPulls,startRate, endRate, step,instancesPerRate);
    waitMessage.delete().queue();
    event.getChannel().sendMessage(result).queue();
  }
  
  public static String getResult(Integer totalPulls, Integer fiveStarPulls, double startRate, double endRate, double step, int instancesPerRate) {


    int currentDataTotal = totalPulls;
    int currentDataFiveStars = fiveStarPulls;
    int spread = Double.valueOf(currentDataFiveStars*0.015).intValue();

    Map<Double, Long> results = DoubleStream.iterate(startRate, d -> d + step).limit(Double.valueOf((endRate-startRate)/step).longValue()).mapToObj(rate -> rate)
        .collect(Collectors.toMap(rate -> rate,
            rate -> IntStream.range(0, instancesPerRate)
                .mapToObj(instance -> IntStream.range(0, currentDataTotal).filter(p -> Math.random() < rate).count())
                .filter(c -> Math.abs(c - currentDataFiveStars) <= spread).count()));

    StringBuilder sb = new StringBuilder();
    sb.append("***Current Summary***\n");
    sb.append("Gold Keys pulled: "+currentDataTotal+"\n");
    sb.append("5\\*s pulled: "+currentDataFiveStars+"\n");
    sb.append("\n");
    sb.append("For each possible rate, we simulated "+currentDataTotal+" key pulls "+instancesPerRate+" times and recorded how often the successes came within 1.5% of our current pulls.\n");
    sb.append("This is the resulting ~~boob diagram~~ histogram:\n");
    sb.append("``"+"`"); //just so I can copy this code to discord
    sb.append(getHistogram(results,20));
    sb.append("``"+"`");
    return sb.toString();
  }

  public static String getHistogram(Map<Double, Long> results, int width) {
    Long maxVal = results.entrySet().stream().max((e1, e2) -> e1.getValue().compareTo(e2.getValue())).get().getValue();
    return results.entrySet().stream().filter(e->e.getValue()>0)
        .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
        .map(e -> String.format("%1.2f |%s  %d", 
            100*e.getKey(), 
            IntStream.range(0, width * e.getValue().intValue() / maxVal.intValue()).mapToObj(i -> "*").collect(Collectors.joining()),
            e.getValue()))
        .collect(Collectors.joining("\n"));
  }

  @Override
  protected String shortInfoInternal() {
    return "Do some analysis about gold key pulls";
  }

  @Override
  protected String usageInternal() {
    return commandString("<NUMBER_OF_PULLS> <NUMBER_OF_FIVE_STARS> [START_RATE] [END_RATE] [STEP] [INSTANCES_PER_RATE]")+"\n";
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
