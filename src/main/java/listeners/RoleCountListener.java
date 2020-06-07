package listeners;

import java.util.List;
import java.util.Optional;

import containers.CommandMessage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RoleCountListener extends AbstractMessageListener {

  public RoleCountListener(JDA jda) {
    super(jda, "rolecount");
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

}
