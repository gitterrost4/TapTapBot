// $Id $
// (C) cantamen/Paul Kramer 2020
package de.gitterrost4.taptapbot.listeners;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.gitterrost4.botlib.helpers.Emoji;
import de.gitterrost4.botlib.listeners.AbstractListener;
import de.gitterrost4.taptapbot.config.containers.ServerConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

/**
 * TODO documentation
 */
public class WelcomeListener extends AbstractListener<ServerConfig> {

  public WelcomeListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getWelcomeConfig());
    Timer t = new Timer();
    t.scheduleAtFixedRate(new WelcomeKicker(), 10000, 86400000);
  }

  @Override
  public void messageReactionAdd(MessageReactionAddEvent event) {
    if (!event.getChannel().getId().equals(config.getWelcomeConfig().getChannelId())) {
      return;
    }

    if (event.getReactionEmote().isEmoji() && event.getReactionEmote().getEmoji().equals(Emoji.ROBOT.asString())) {
      event.getGuild().addRoleToMember(event.getMember(),
          event.getGuild().getRoleById(config.getWelcomeConfig().getAndroidRoleId())).queue();
      event.getGuild()
          .addRoleToMember(event.getMember(), event.getGuild().getRoleById(config.getWelcomeConfig().getMemberRoleId()))
          .queue();
      event.getGuild().removeRoleFromMember(event.getMember(),
          event.getGuild().getRoleById(config.getWelcomeConfig().getWelcomeRoleId())).queue();
      event.getChannel().retrieveMessageById(event.getMessageId())
          .queue(message -> message.removeReaction(Emoji.ROBOT.asString(), event.getUser()).queue());
    }

    if (event.getReactionEmote().isEmoji() && event.getReactionEmote().getEmoji().equals(Emoji.APPLE.asString())) {
      event.getGuild()
          .addRoleToMember(event.getMember(), event.getGuild().getRoleById(config.getWelcomeConfig().getIosRoleId()))
          .queue();
      event.getGuild()
          .addRoleToMember(event.getMember(), event.getGuild().getRoleById(config.getWelcomeConfig().getMemberRoleId()))
          .queue();
      event.getGuild().removeRoleFromMember(event.getMember(),
          event.getGuild().getRoleById(config.getWelcomeConfig().getWelcomeRoleId())).queue();
      event.getChannel().retrieveMessageById(event.getMessageId())
          .queue(message -> message.removeReaction(Emoji.APPLE.asString(), event.getUser()).queue());
    }
  }
  
  @Override
  public void onGuildMemberJoin(GuildMemberJoinEvent event) {
    super.onGuildMemberJoin(event);
    event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(config.getWelcomeConfig().getWelcomeRoleId())).queue();
    event.getMember().getUser().openPrivateChannel().queue(ch->ch.sendMessage(
        "***Important Notice:***\n"
        + "Dear user. This is not a normal 'welcome to the server'-Message. This message is to inform you that the"
        + "Developers of this game have lied to the playerbase for multiple years about the drop rates of the Golden Chests.\n"
        + "I want every user to be informed about this, so they can really decide if they want to spend money on this game.\n"
        + "For more information about this issue look at the #discord-announcements channel after joining the server or message @gitterrost4#4912 (the admin of this discord) directly.\n\n"
        + "And now: Welcome to the server! :-)"
        ).queue());
  }

  private class WelcomeKicker extends TimerTask {

    @Override
    public void run() {
      List<Member> members = guild().getMembersWithRoles(guild().getRolesByName("Welcome", false));
      members.stream().filter(m -> m.getRoles().size() == 1 && m.getRoles().get(0).getName().equals("Welcome"))
          .filter(m -> m.getTimeJoined().isBefore(OffsetDateTime.now().minusDays(7)))
          .forEach(m -> m.kick("Not actually registered").queue());
    }
  }
}

// end of file
