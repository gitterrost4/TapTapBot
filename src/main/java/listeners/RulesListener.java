package listeners;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import config.Config;
import containers.CommandMessage;
import helpers.Catcher;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RulesListener extends AbstractMessageListener {

  public RulesListener(JDA jda) {
    super(jda,"rules");
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event,CommandMessage messageContent) {
    if (!event.getMember().hasPermission(jda.getGuildChannelById(Config.get("rules.channelId")),
      Permission.MESSAGE_WRITE,Permission.MESSAGE_MANAGE)) {
      return;
    }
    System.err.println("messageContent: '" + messageContent.getArg(0).get() + "'");
    if (messageContent.getArg(0).map(arg -> arg.equals("generate")).orElse(false)) {
      event.getChannel().sendMessage("Deleting all messages from the rules channel").queue();
      TextChannel channel=jda.getTextChannelById(Config.get("rules.channelId"));
      deleteAllMessagesFromChannel(channel,
        () -> event.getChannel().sendMessage("Done deleting all messages.").queue(unused -> sendAllRules(channel)));
      return;
    }

    event.getChannel().sendMessage("No command given!").queue();
  }

  private void sendAllRules(TextChannel channel) {
    ClassLoader loader=Thread.currentThread().getContextClassLoader();
    URL url=loader.getResource("rules");
    String path=url.getPath();
    List<File> files=Stream.of(new File(path).listFiles()).sorted((f1,f2) -> f1.getName().compareTo(f2.getName()))
      .collect(Collectors.toList());
    String sep="";
    StringBuilder builder=new StringBuilder("Please scroll to find your language!\n");
    for (File file : files) {
      String fileContent=Catcher.wrap(() -> new BufferedReader(new InputStreamReader(new FileInputStream(file)))
        .lines().collect(Collectors.joining("\n")));
      String country=file.getName().substring(file.getName().length() - 2);
      String text=sep + ":flag_" + country + ": :flag_" + country + ": :flag_" + country + ":\n" + fileContent + "\n";
      
      splitString(text,2000).stream().sequential().forEach(msg->channel.sendMessage(msg).queue());
      sep="~~-                                                                                             -~~\n";
      builder.append(":flag_" + country + ": ");
    }
    channel.sendMessage(sep + builder.toString()).queue();
  }
  
  private static List<String> splitString(String string, Integer limit){
    if(string.length()<limit) {
      return Stream.of(string).collect(Collectors.toList());
    }
    List<String> result = new ArrayList<>();
    while(string.length()>=limit) {
      result.add(0,string.substring(string.indexOf("\n",string.length()-limit)+1));
      string = string.substring(0,string.indexOf("\n",string.length()-limit)+1);
    }
    result.add(0,string);
    return result;
  }

  private void deleteAllMessagesFromChannel(TextChannel channel,Runnable fin) {
    channel.getHistoryFromBeginning(50).queue(history -> {
      List<Message> messages=history.getRetrievedHistory();
      if (!messages.isEmpty()) {
        channel.deleteMessages(messages).queue(unused -> deleteAllMessagesFromChannel(channel,fin));
      } else {
        fin.run();
      }
    });
  }

}
