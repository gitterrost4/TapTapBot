// $Id $
// (C) cantamen/Paul Kramer 2020
package listeners.modtools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import config.Config;
import containers.ChoiceMenu;
import containers.ChoiceMenu.ChoiceMenuBuilder;
import listeners.AbstractMessageListener;
import containers.CommandMessage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

/**
 * TODO documentation
 */
public class UnmuteListener extends AbstractMessageListener {

  public Map<String,ChoiceMenu> activeMenus=new HashMap<>();

  public UnmuteListener(JDA jda) {
    super(jda,"unmute");
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event,CommandMessage messageContent) {
    event.getMessage().delete().queue();
    if (!guild().getMember(event.getAuthor()).hasPermission(Permission.MANAGE_ROLES)) {
      event.getChannel().sendMessage("***You don't have the right to unmute people!***").queue();
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
      .filter(m->m.getRoles().stream().anyMatch(r->r.getId().equals(Config.get("mute.muteRoleId"))))
      .collect(Collectors.toSet()));

    if(possibleMembers.size()==0) {
      event.getChannel().sendMessage("***No muted user found for input "+messageContent.getArg(0).get()+"***").queue();
      return;      
    }
    
    if(possibleMembers.size()>10) {
      event.getChannel().sendMessage("***Too many muted users found for input "+messageContent.getArg(0).get()+". Please be more specific.***").queue();
      return;      
    }      

    ChoiceMenuBuilder builder=ChoiceMenu.builder();

    possibleMembers.stream().map(m -> new ChoiceMenu.MenuEntry(m.getUser().getAsTag(),m.getId()))
      .forEach(builder::addEntry);
    builder.setChoiceHandler(
      e -> guild().removeRoleFromMember(guild().getMemberById(e.getValue()),guild().getRoleById(Config.get("mute.muteRoleId"))).queue(x -> event
        .getChannel().sendMessage("***Unmuted member " + jda.getUserById(e.getValue()).getAsTag() + "***").queue()));
    builder.setTitle("Unmute member");
    builder.setDescription("Choose a member to be unmuted");

    ChoiceMenu menu=builder.build();
    activeMenus.put(menu.display(event.getChannel()),menu);
  }

  @Override
  protected void messageReactionAdd(MessageReactionAddEvent event) {
    super.messageReactionAdd(event);
    if (activeMenus.containsKey(event.getMessageId())) {
      activeMenus.get(event.getMessageId()).handleReaction(event);
    }
  }

}

// end of file
