// $Id $
// (C) cantamen/Paul Kramer 2019
package listeners;

import java.math.BigDecimal;
import java.math.BigInteger;

import config.containers.ServerConfig;
import containers.CommandMessage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * TODO documentation
 */
public class CalculateListener extends AbstractMessageListener {

  public CalculateListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getCalculateConfig(), "calc");
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    if (!messageContent.getArg(0).isPresent()) {
      event.getChannel().sendMessage("Not enough arguments").queue();
      return;
    }
    String command = messageContent.getArg(0).get();
    switch (command) {
    case "pulls":
      try {
        Integer numberOfPulls = messageContent.getArg(1).map(Integer::parseInt).orElse(500);
        if (numberOfPulls > 10000) {
          event.getChannel().sendMessage("Number of pulls may not exceed 10000.").queue();
          return;
        }
        Double probabilityOfSuccessEach = messageContent.getArg(2).map(Double::parseDouble).orElse(0.004);
        String result = "";
        double total = 0;
        int maxSuccess = 5;
        for (int numSuccess = 0; numSuccess <= maxSuccess; numSuccess++) {
          Double prob = new BigDecimal(choose(numberOfPulls, numSuccess))
              .multiply(new BigDecimal(probabilityOfSuccessEach).pow(numSuccess))
              .multiply(new BigDecimal(1 - probabilityOfSuccessEach).pow(numberOfPulls - numSuccess)).doubleValue();
          total += prob;
          result += String.format(
              "You have a %.1f%% chance of getting %d of the desired outcome. (that's at least 1 in %.0f)%n",
              prob * 100, numSuccess, Math.ceil(1 / prob));
        }
        result += String.format("You have a %.1f%% chance of getting more than %d of the desired outcome.%n",
            (1 - total) * 100, maxSuccess);
        event.getChannel().sendMessage(String.format("Probabilities for %d pulls with a success rate of %.1f%% each%n",
            numberOfPulls, probabilityOfSuccessEach * 100) + result).queue();
      } catch (Exception e) {
        event.getChannel().sendMessage("Error during calculation. Maybe your numbers are too big?").queue();
        throw e;
      }
      break;
    default:
      event.getChannel().sendMessage("Unknown command `" + command + "`").queue();
      return;
    }
  }

  @Override
  protected String shortInfoInternal() {
    return "Calculate probabilities in the game";
  }

  @Override
  protected String usageInternal() {
    return commandString("pulls [NUMBER_OF_PULLS] [PROBABILITY_OF_SUCCESS]`");
  }

  @Override
  protected String descriptionInternal() {
    return "Calculate the probability of getting a good outcome where each success has a probability of PROBABILITY_OF_SUCCESS and you are doing NUMBER_OF_PULLS pulls.";
  }

  @Override
  protected String examplesInternal() {
    return commandString("")+"\n"
        + "Gives information about the probabilities of getting the desired outcome with probability 0.4% in 500 pulls\n"
        + commandString("1000 0.2")+"\n"
        + "Gives information about the probabilities of getting the desired outcome with probability 20% in 1000 pulls\n";
  }

  public static BigInteger choose(int x, int y) {
    if (y < 0 || y > x)
      return BigInteger.ZERO;
    if (y == 0 || y == x)
      return BigInteger.ONE;

    BigInteger answer = BigInteger.ONE;
    for (int i = x - y + 1; i <= x; i++) {
      answer = answer.multiply(BigInteger.valueOf(i));
    }
    for (int j = 1; j <= y; j++) {
      answer = answer.divide(BigInteger.valueOf(j));
    }
    return answer;
  }
}

// end of file
