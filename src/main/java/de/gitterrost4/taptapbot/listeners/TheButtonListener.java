package de.gitterrost4.taptapbot.listeners;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.gitterrost4.botlib.containers.CommandMessage;
import de.gitterrost4.botlib.listeners.AbstractMessageListener;
import de.gitterrost4.taptapbot.config.containers.ServerConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;

public class TheButtonListener extends AbstractMessageListener<ServerConfig>{

  public TheButtonListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getTheButtonConfig(), "thebutton");
    // TODO Auto-generated constructor stub
  }

  @Override
  protected boolean hasAccess(Member member) {
    return member.hasPermission(Permission.ADMINISTRATOR);
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage message) {
    event.getChannel().sendMessage("**The Button**")
      .setActionRow(Button.secondary("thebutton", "This Button has been clicked 0 times."))
      .queue();
  }
  
  @Override
  protected void buttonClick(ButtonClickEvent event) {
    if(event.getComponentId().equals("thebutton")) {
      String currentLabel = event.getButton().getLabel();
      Optional<Integer> currentTimes = extractNumberFromString(currentLabel);
      currentTimes.ifPresent(num->{
      event.editButton(Button.secondary("thebutton", "This Button has been clicked "+(num+1)+" times."+Optional.ofNullable(getSuffix(num+1)).map(s->" ("+s+")").orElse(""))).queue();
      });
    }
  }
  
  private static Optional<Integer> extractNumberFromString(String str){
    Pattern numberPat = Pattern.compile("\\d+");
    Matcher matcher1 = numberPat.matcher(str);
    if(!matcher1.find()) { return Optional.empty(); }
    return Optional.of(Integer.parseInt(matcher1.group()));
  }
  
  private static String getSuffix(Integer num) {
    switch(num) {
    case 42: return "the answer to everything";
    case 69: return "nice";
    case 300: return "THIS IS SPARTAAAA";
    case 404: return "not found";
    case 420: return "blaze it";
    case 666: return "hey satan";
    case 6969: return "double nice";
    default:
      return null;
    }
  }
  
  
  

}
