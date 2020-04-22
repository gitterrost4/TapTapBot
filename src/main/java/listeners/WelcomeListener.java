// $Id $
// (C) cantamen/Paul Kramer 2020
package listeners;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import config.Config;
import helpers.Emoji;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

/**
 * TODO documentation
 */
public class WelcomeListener extends AbstractListener {

  public WelcomeListener(JDA jda) {
    super(jda);
    Timer t = new Timer();
    t.scheduleAtFixedRate(new Unmuter(), 10000, 86400000);
  }

  @Override
  public void onGuildMemberJoin(GuildMemberJoinEvent event) {
    super.onGuildMemberJoin(event);
    if (event.getMember().getUser().getId().equals("676132951951671299")) {
      guild().addRoleToMember(event.getMember(), guild().getRoleById("426232962728722434")).queue();
    }
//    EmbedBuilder builder = new EmbedBuilder();
//    Optional.ofNullable(event.getMember())
//        .map(a -> builder.setAuthor(a.getEffectiveName(), null, a.getUser().getEffectiveAvatarUrl()));
//    builder.addField("Member joined", event.getMember().getUser().getAsTag(), false);
//    builder.addField("Account created", event.getMember().getUser().getTimeCreated().toString() + " ("
//        + Duration.between(event.getMember().getUser().getTimeCreated(), OffsetDateTime.now()) + " ago)", false);
//    guild().getMemberById("162115267957096448").getUser().openPrivateChannel()
//        .queue(channel -> channel.sendMessage(builder.build()).queue());
  }

  @Override
  public void messageReactionAdd(MessageReactionAddEvent event) {
    if (!event.getChannel().getId().equals(Config.get("welcome.channelId"))) {
      return;
    }

    if (event.getReactionEmote().isEmoji() && event.getReactionEmote().getEmoji().equals(Emoji.ROBOT.asString())) {
      event.getGuild()
          .addRoleToMember(event.getMember(), event.getGuild().getRoleById(Config.get("welcome.androidRoleId")))
          .queue();
      event.getGuild()
          .addRoleToMember(event.getMember(), event.getGuild().getRoleById(Config.get("welcome.memberRoleId"))).queue();
      event.getGuild()
          .removeRoleFromMember(event.getMember(), event.getGuild().getRoleById(Config.get("welcome.welcomeRoleId")))
          .queue();
      event.getChannel().retrieveMessageById(event.getMessageId())
          .queue(message -> message.removeReaction(Emoji.ROBOT.asString(), event.getUser()).queue());
    }

    if (event.getReactionEmote().isEmoji() && event.getReactionEmote().getEmoji().equals(Emoji.APPLE.asString())) {
      event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(Config.get("welcome.iosRoleId")))
          .queue();
      event.getGuild()
          .addRoleToMember(event.getMember(), event.getGuild().getRoleById(Config.get("welcome.memberRoleId"))).queue();
      event.getGuild()
          .removeRoleFromMember(event.getMember(), event.getGuild().getRoleById(Config.get("welcome.welcomeRoleId")))
          .queue();
      event.getChannel().retrieveMessageById(event.getMessageId())
          .queue(message -> message.removeReaction(Emoji.APPLE.asString(), event.getUser()).queue());
    }
  }

  private class Unmuter extends TimerTask {

    @Override
    public void run() {
      List<Member> members = guild().getMembersWithRoles(guild().getRolesByName("Welcome", false));
      members.stream().filter(m -> m.getRoles().size() == 1 && m.getRoles().get(0).getName().equals("Welcome"))
          .filter(m -> m.getTimeJoined().isBefore(OffsetDateTime.now().minusDays(7)))
//          .forEach(m -> System.err.println("I would kick " + m.getUser().getAsTag() + " joined " + m.getTimeJoined()
//              + " with the roles " + m.getRoles()));
          .forEach(m -> m.kick("Not actually registered").queue());
    }
  }
}

// end of file
