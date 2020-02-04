// $Id $
// (C) cantamen/Paul Kramer 2020
package listeners.modtools;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import containers.ChoiceMenu;
import containers.ChoiceMenu.ChoiceMenuBuilder;
import containers.CommandMessage;
import database.ConnectionHelper;
import listeners.AbstractMessageListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

/**
 * TODO documentation
 */
public class BanListener extends AbstractMessageListener {

  public Map<String,ChoiceMenu> activeMenus=new HashMap<>();

  public BanListener(JDA jda) {
    super(jda,"ban");
    ConnectionHelper.update(
      "create table if not exists bannedmembers(id INTEGER PRIMARY KEY not null, userid varchar(255) not null, banneduntil text null);");
    Timer t = new Timer();
    t.scheduleAtFixedRate(new Unbanner(),10000,10000);
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event,CommandMessage messageContent) {
    event.getMessage().delete().queue();
    if (!guild().getMember(event.getAuthor()).hasPermission(Permission.BAN_MEMBERS)) {
      event.getChannel().sendMessage("***You don't have the right to ban people!***").queue();
      return;
    }
    if (!messageContent.getArg(0).isPresent()) {
      event.getChannel().sendMessage("***No user given!***").queue();
      return;
    }
    
    Set<Member> possibleMembers=new HashSet<>();
    possibleMembers.addAll(event.getMessage().getMentionedMembers());
    possibleMembers.addAll(guild().getMembers().stream()
      .filter(m -> m.getEffectiveName().toLowerCase().contains(messageContent.getArg(0).get().toLowerCase())
        || m.getUser().getAsTag().contains(messageContent.getArg(0).get()))
      .collect(Collectors.toSet()));
    
    ChoiceMenuBuilder builder=ChoiceMenu.builder();

    if(possibleMembers.size()==0) {
      event.getChannel().sendMessage("***No user found for input "+messageContent.getArg(0).get()+"***").queue();
      return;      
    }

    if(possibleMembers.size()>10) {
      event.getChannel().sendMessage("***Too many users found for input "+messageContent.getArg(0).get()+". Please be more specific.***").queue();
      return;      
    }
    Optional<Duration> oDuration=messageContent.getArg(1).map(String::toUpperCase).map(durationString -> {
      if (durationString.contains("D")) {
        return durationString.replaceFirst("\\d+D","P$0T");
      } else {
        return "PT" + durationString;
      }
    }).map(Duration::parse);
    String durationString=oDuration.map(d->" for "+d.toString()
    .substring(2)
    .replaceAll("(\\d[HMS])(?!$)", "$1 ")
    .toLowerCase()).orElse("");
    possibleMembers.stream().map(m -> new ChoiceMenu.MenuEntry(m.getUser().getAsTag(),m.getId()))
      .forEach(builder::addEntry);
    builder.setChoiceHandler(
      e -> guild().ban(guild().getMemberById(e.getValue()),6).queue(x -> {
        event.getChannel().sendMessage("***Banned member " + jda.getUserById(e.getValue()).getAsTag() + durationString+"***").queue();
        oDuration
          .ifPresent(duration -> ConnectionHelper.update("insert into bannedmembers (userid, muteduntil) VALUES (?,?)",
            e.getValue(),Instant.now().plus(duration).toString()));
      }));
    builder.setTitle("Ban member");
    builder.setDescription("Choose a member to be banned"+durationString);

    ChoiceMenu menu=builder.build();
    activeMenus.put(menu.display(event.getChannel()),menu);
  }

  @Override
  protected void messageReactionAdd(MessageReactionAddEvent event) {
    super.messageReactionAdd(event);
    if (activeMenus.containsKey(event.getMessageId())) {
      if (!guild().getMember(event.getUser()).hasPermission(Permission.BAN_MEMBERS)) {
        return;
      }
      activeMenus.get(event.getMessageId()).handleReaction(event);
    }
  }
  
  private class Unbanner extends TimerTask{

    @Override
    public void run() {
      List<String> users=ConnectionHelper.getResults("select userid from bannedmembers where banneduntil<?",
        rs -> rs.getString("userid"),Instant.now().toString());
      users.forEach(u -> guild().unban(u)
        .queue(unused -> ConnectionHelper.update("delete from bannedmembers where userid=?",u)));      
    }
  }

}

// end of file
