package listeners.modtools;

import java.util.Optional;
import java.util.stream.Collectors;

import config.Config;
import containers.CommandMessage;
import database.ConnectionHelper;
import listeners.AbstractMessageListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;

public class RoleListener extends AbstractMessageListener {

  public RoleListener(JDA jda) {
    super(jda, "role");
    ConnectionHelper.update(
        "create table if not exists roleassignments(id INTEGER PRIMARY KEY not null, emoji text not null, rolename text not null);");
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    if (!guild().getMember(event.getAuthor()).hasPermission(Permission.MANAGE_ROLES)) {
      event.getChannel().sendMessage("***You don't have the right to create roles!***").queue();
      return;
    }

    switch (messageContent.getArg(0).orElse(null)) {
    case "add":
      String emoji = messageContent.getArg(1).orElseThrow(() -> new IllegalArgumentException("not enough arguments"));
      String roleName = messageContent.getArg(2)
          .orElseThrow(() -> new IllegalArgumentException("not enough arguments"));
      ConnectionHelper.update("insert into roleassignments(emoji, rolename) VALUES (?,?)", emoji, roleName);
      event.getChannel().sendMessage("Role assignment added. React with " + emoji + " to assign the role " + roleName)
          .queue();
      break;
    case "delete":
      String delEmoji = messageContent.getArg(1)
          .orElseThrow(() -> new IllegalArgumentException("not enough arguments"));
      ConnectionHelper.update("delete from roleassignments where emoji=?", delEmoji);
      event.getChannel().sendMessage("Role assignment for " + delEmoji + " deleted.").queue();
      break;
    case "list":
      String list = ConnectionHelper.getResults("select emoji, rolename from roleassignments",
          rs -> rs.getString("emoji") + ": " + rs.getString("rolename")).stream().collect(Collectors.joining("\n"));
      event.getChannel().sendMessage(list).queue();
      break;
    }
  }

  @Override
  protected void messageReactionAdd(MessageReactionAddEvent event) {
    super.messageReactionAdd(event);
    if(!event.getChannel().getId().equals(Config.get("role.reactionChannel"))) {
      return;
    }
    Optional<String> oRoleName = ConnectionHelper.getFirstResult("select rolename from roleassignments where emoji=?",
        rs -> rs.getString("rolename"), event.getReactionEmote().getEmoji());
    oRoleName.ifPresent(roleName -> {
      Member member = event.getMember();
      guild().addRoleToMember(member, guild().getRolesByName(roleName, true).stream().findFirst()
          .orElseThrow(() -> new IllegalStateException("role " + roleName + " doesn't exist"))).queue();
      member.getUser().openPrivateChannel().queue(channel->channel.sendMessage("You assigned the role "+roleName+" to yourself.").queue());
    });
  }

  @Override
  protected void messageReactionRemove(MessageReactionRemoveEvent event) {
    super.messageReactionRemove(event);
    if(!event.getChannel().getId().equals(Config.get("role.reactionChannel"))) {
      return;
    }
    Optional<String> oRoleName = ConnectionHelper.getFirstResult("select rolename from roleassignments where emoji=?",
        rs -> rs.getString("rolename"), event.getReactionEmote().getEmoji());
    oRoleName.ifPresent(roleName -> {
      Member member = event.getMember();
      guild().removeRoleFromMember(member, guild().getRolesByName(roleName, true).stream().findFirst()
          .orElseThrow(() -> new IllegalStateException("role " + roleName + " doesn't exist"))).queue();
      member.getUser().openPrivateChannel().queue(channel->channel.sendMessage("You removed the role "+roleName+" from yourself.").queue());
    });
  }

  
}
