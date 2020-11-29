package listeners.modtools;

import java.util.Optional;
import java.util.stream.Collectors;

import config.containers.ServerConfig;
import containers.CommandMessage;
import listeners.AbstractMessageListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;

public class RoleListener extends AbstractMessageListener {

  public RoleListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getRoleConfig(), "role");
    connectionHelper.update(
        "create table if not exists roleassignments(id INTEGER PRIMARY KEY not null, emoji text not null, rolename text not null);");
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    if (!hasAccess(guild().getMember(event.getAuthor()))) {
      event.getChannel().sendMessage("***You don't have the right to create roles!***").queue();
      return;
    }

    switch (messageContent.getArg(0).orElse(null)) {
    case "add":
      String emoji = messageContent.getArg(1).orElseThrow(() -> new IllegalArgumentException("not enough arguments"));
      String roleName = messageContent.getArg(2)
          .orElseThrow(() -> new IllegalArgumentException("not enough arguments"));
      connectionHelper.update("insert into roleassignments(emoji, rolename) VALUES (?,?)", emoji, roleName);
      event.getChannel().sendMessage("Role assignment added. React with " + emoji + " to assign the role " + roleName)
          .queue();
      break;
    case "delete":
      String delEmoji = messageContent.getArg(1)
          .orElseThrow(() -> new IllegalArgumentException("not enough arguments"));
      connectionHelper.update("delete from roleassignments where emoji=?", delEmoji);
      event.getChannel().sendMessage("Role assignment for " + delEmoji + " deleted.").queue();
      break;
    case "list":
      String list = connectionHelper.getResults("select emoji, rolename from roleassignments",
          rs -> rs.getString("emoji") + ": " + rs.getString("rolename")).stream().collect(Collectors.joining("\n"));
      event.getChannel().sendMessage(list).queue();
      break;
    }
  }

  @Override
  protected boolean hasAccess(Member member) {
    return member.hasPermission(Permission.MANAGE_ROLES);
  }

  @Override
  protected void messageReactionAdd(MessageReactionAddEvent event) {
    super.messageReactionAdd(event);
    if (!event.getChannel().getId().equals(config.getRoleConfig().getReactionChannelId())) {
      return;
    }
    Optional<String> oRoleName = connectionHelper.getFirstResult("select rolename from roleassignments where emoji=?",
        rs -> rs.getString("rolename"), event.getReactionEmote().getEmoji());
    oRoleName.ifPresent(roleName -> {
      Member member = event.getMember();
      guild().addRoleToMember(member, guild().getRolesByName(roleName, true).stream().findFirst()
          .orElseThrow(() -> new IllegalStateException("role " + roleName + " doesn't exist"))).queue();
      member.getUser().openPrivateChannel()
          .queue(channel -> channel.sendMessage("You assigned the role " + roleName + " to yourself.").queue());
    });
  }

  @Override
  protected void messageReactionRemove(MessageReactionRemoveEvent event) {
    super.messageReactionRemove(event);
    if (!event.getChannel().getId().equals(config.getRoleConfig().getReactionChannelId())) {
      return;
    }
    Optional<String> oRoleName = connectionHelper.getFirstResult("select rolename from roleassignments where emoji=?",
        rs -> rs.getString("rolename"), event.getReactionEmote().getEmoji());
    oRoleName.ifPresent(roleName -> {
      Member member = event.getMember();
      guild().removeRoleFromMember(member, guild().getRolesByName(roleName, true).stream().findFirst()
          .orElseThrow(() -> new IllegalStateException("role " + roleName + " doesn't exist"))).queue();
      member.getUser().openPrivateChannel()
          .queue(channel -> channel.sendMessage("You removed the role " + roleName + " from yourself.").queue());
    });
  }

  @Override
  protected String shortInfoInternal() {
    return "Create, delete or list roles that members can assign to themselves.";
  }

  @Override
  protected String usageInternal() {
    return commandString("add <EMOJI> <ROLE_NAME>") + "\n" + commandString("delete <EMOJI>") + "\n"
        + commandString("list");
  }

  @Override
  protected String descriptionInternal() {
    return "Use the `add` command to add a self-assignable role by giving the EMOJI that should be the reaction and the ROLE_NAME that should be assigned.\n"
        + "Use the `delete` command to delete a self-assignable role by giving the corresponding EMOJI.\n"
        + "Use the `list` command to list all currently self-assignable roles.";
  }

  @Override
  protected String examplesInternal() {
    return commandString("add :smirk: coolRole") + "\n"
        + "Make the role `coolRole` self-assignable by reacting to the assign-message with the Smirk emoji.\n"
        + commandString("delete :smirk:") + "\n" + "Make the role not self-assignable anymore.\n"
        + commandString("list") + "\n" + "List all currently self-assignable roles.";
  }

}
