package listeners;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import config.containers.ServerConfig;
import containers.CommandMessage;
import database.ConnectionHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class AutoRespondListener extends AbstractMessageListener {

  public AutoRespondListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getAutoRespondConfig(), "autorespond");
    connectionHelper.update(
        "create table if not exists autoresponse(id INTEGER PRIMARY KEY not null, name text not null UNIQUE, pattern text not null, response text not null);");
    jda.addEventListener(new AutoResponder(jda, guild, config));
  }

  @Override
  protected boolean hasAccess(Member member) {
    return member.getRoles().stream()
        .anyMatch(r -> r.compareTo(guild().getRoleById(config.getAutoRespondConfig().getMinRoleId())) >= 0);
  }

  @Override
  protected String shortInfoInternal() {
    return "Add or delete autoresponses";
  }

  @Override
  protected String usageInternal() {
    return commandString("add <NAME> <REGEX> <RESPONSE>")+"\n" + commandString("delete <NAME>")+"\n"
        + commandString("list");
  }

  @Override
  protected String descriptionInternal() {
    return "Adds, deletes or lists an autoresponse. The response needs to get an alphanumeric NAME (no spaces). Every ingoing message will then be checked against the given regular expression. If it matches, the response will be sent by the bot to the same channel.";
  }

  @Override
  protected String examplesInternal() {
    return commandString("add !test !test This is a test message")+"\n"
        + "Adds a !test command that always gives the message `This is a test message`.\n"  
        + commandString("add giftcode (any|where|does).*(gift|code) Giftcodes can be found in the side bar")+"\n"
        + "Adds a reaction to someone asking for giftcodes.\n" 
        + commandString("list")+"\n"
        + "Lists all autoresponses.";
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    if (!hasAccess(event.getMember())) {
      return;
    }
    if (messageContent.getArg(0).filter(x -> x.equals("add")).isPresent()) {
      String name = messageContent.getArg(1).orElseThrow(() -> new IllegalArgumentException("no name supplied"));
      String pattern = messageContent.getArg(2).orElseThrow(() -> new IllegalArgumentException("no pattern supplied"));
      String response = messageContent.getArg(3, true)
          .orElseThrow(() -> new IllegalArgumentException("no response supplied"));
      connectionHelper.update("REPLACE INTO autoresponse (name, pattern, response) VALUES (?,?,?)", name, pattern,
          response);
      event.getChannel().sendMessage("Auto-Response " + name + " added with Pattern `" + pattern + "`").queue();
    } else if (messageContent.getArg(0).filter(x -> x.equals("list")).isPresent()) {
      List<Map<String, String>> responses = getResponses(connectionHelper);
      responses.forEach(resp -> event.getChannel()
          .sendMessage(new EmbedBuilder().addField("Name", resp.get("name"), false)
              .addField("Pattern", "`" + resp.get("pattern") + "`", false)
              .addField("Response",
                  resp.get("response").length() > 1020 ? resp.get("response").substring(0, 1020) + "..."
                      : resp.get("response"),
                  false)
              .build())
          .queue());
    } else if (messageContent.getArg(0).filter(x -> x.equals("delete")).isPresent()) {
      String name = messageContent.getArg(1).orElseThrow(() -> new IllegalArgumentException("no name supplied"));
      connectionHelper.update("DELETE FROM autoresponse where name=?", name);
      event.getChannel().sendMessage("Auto-Response " + name + " deleted").queue();
    }
  }

  private static List<Map<String, String>> getResponses(ConnectionHelper connectionHelper) {
    List<Map<String, String>> responses = connectionHelper.getResults(
        "select name, pattern, response from autoresponse",
        rs -> Stream
            .of(new SimpleEntry<>("name", rs.getString("name")), new SimpleEntry<>("pattern", rs.getString("pattern")),
                new SimpleEntry<>("response", rs.getString("response")))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
    return responses;
  }

  private static class AutoResponder extends AbstractListener {

    public AutoResponder(JDA jda, Guild guild, ServerConfig config) {
      super(jda, guild, config, config.getAutoRespondConfig());
    }

    @Override
    protected void messageReceived(MessageReceivedEvent event) {
      super.messageReceived(event);
      List<Map<String, String>> responses = getResponses(connectionHelper);
      responses.stream()
          .filter(resp -> Pattern.matches("(?i).*" + resp.get("pattern") + ".*", event.getMessage().getContentRaw()))
          .findFirst().ifPresent(resp -> event.getChannel().sendMessage(resp.get("response")).queue());
      ;
    }

  }

}
