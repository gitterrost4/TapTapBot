package listeners;

import java.util.List;
import java.util.Optional;

import config.containers.ServerConfig;
import containers.CommandMessage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RoleCountListener extends AbstractMessageListener {

  public RoleCountListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getRoleCountConfig(), "rolecount");
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    Optional<String> roleName = messageContent.getArg(0, true);
    roleName.ifPresent(rn -> {
      List<Role> roles = guild().getRolesByName(rn, false);
      int num = guild().getMembersWithRoles(roles).size();
      event.getChannel().sendMessage("There are " + num + " members with the role " + rn).queue();
    });
  }

  @Override
  protected String shortInfoInternal() {
    return "Display the number of members that have a specified role.";
  }

  @Override
  protected String usageInternal() {
    return commandString("<ROLE_NAME>");
  }

  @Override
  protected String descriptionInternal() {
    return "Display the number of members that have the role specified by ROLE_NAME. Capitalization does not matter here.";
  }

  @Override
  protected String examplesInternal() {
    return commandString("Moderator")+"\n"
        + "Show how many moderators there are on the discord.";
  }
  
  

}
