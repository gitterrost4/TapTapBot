// $Id $
// (C) cantamen/Paul Kramer 2020
package listeners;

import config.Config;
import helpers.Emoji;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

/** 
 * TODO documentation
 */
public class WelcomeListener extends AbstractListener{

  public WelcomeListener(JDA jda) {
    super(jda);
  }

  @Override
  public void messageReactionAdd(MessageReactionAddEvent event) {
    if(!event.getChannel().getId().equals(Config.get("welcome.welcomeChannelId"))) {
      return;
    }
    
    if(event.getReactionEmote().isEmoji()&&event.getReactionEmote().getEmoji().equals(Emoji.ROBOT.asString())) {
      event.getGuild().addRoleToMember(event.getMember(),event.getGuild().getRoleById(Config.get("welcome.androidRoleId"))).queue();
      event.getGuild().addRoleToMember(event.getMember(),event.getGuild().getRoleById(Config.get("welcome.memberRoleId"))).queue();
      event.getGuild().removeRoleFromMember(event.getMember(),event.getGuild().getRoleById(Config.get("welcome.welcomeRoleId"))).queue();
    }

    if(event.getReactionEmote().isEmoji()&&event.getReactionEmote().getEmoji().equals(Emoji.APPLE.asString())) {
      event.getGuild().addRoleToMember(event.getMember(),event.getGuild().getRoleById(Config.get("welcome.iosRoleId"))).queue();
      event.getGuild().addRoleToMember(event.getMember(),event.getGuild().getRoleById(Config.get("welcome.memberRoleId"))).queue();
      event.getGuild().removeRoleFromMember(event.getMember(),event.getGuild().getRoleById(Config.get("welcome.welcomeRoleId"))).queue();
    }
  }

  
  
}


// end of file
