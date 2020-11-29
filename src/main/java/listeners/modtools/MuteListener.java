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

import config.containers.ServerConfig;
import containers.ChoiceMenu;
import containers.ChoiceMenu.ChoiceMenuBuilder;
import containers.CommandMessage;
import listeners.AbstractMessageListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

/**
 * TODO documentation
 */
public class MuteListener extends AbstractMessageListener {

  public Map<String, ChoiceMenu> activeMenus = new HashMap<>();

  public MuteListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getMuteConfig(), "mute");
    connectionHelper.update(
        "create table if not exists mutedmembers(id INTEGER PRIMARY KEY not null, userid varchar(255) not null, muteduntil text null);");
    Timer t = new Timer();
    t.scheduleAtFixedRate(new Unmuter(), 10000, 10000);
  }

  @Override
  public void onGuildMemberJoin(GuildMemberJoinEvent event) {
    super.onGuildMemberJoin(event);
    Optional<Integer> id = connectionHelper.getFirstResult("select id from mutedmembers where userid=?",
        rs -> rs.getInt("id"), event.getUser().getId());
    if (id.isPresent()) {
      guild().addRoleToMember(event.getMember(), guild().getRoleById("426232962728722434")).queue();
    }
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    event.getMessage().delete().queue();
    if (!hasAccess(guild().getMember(event.getAuthor()))) {
      event.getChannel().sendMessage("***You don't have the right to mute people!***").queue();
      return;
    }
    if (!messageContent.getArg(0).isPresent()) {
      event.getChannel().sendMessage("***No user given!***").queue();
      return;
    }

    Set<Member> possibleMembers = new HashSet<>();
    possibleMembers.addAll(event.getMessage().getMentionedMembers());
    possibleMembers.addAll(guild().getMembers().stream()
        .filter(m -> m.getEffectiveName().toLowerCase().contains(messageContent.getArg(0).get().toLowerCase())
            || m.getUser().getAsTag().contains(messageContent.getArg(0).get()))
        .collect(Collectors.toSet()));

    ChoiceMenuBuilder builder = ChoiceMenu.builder();

    if (possibleMembers.size() == 0) {
      event.getChannel().sendMessage("***No user found for input " + messageContent.getArg(0).get() + "***").queue();
      return;
    }

    if (possibleMembers.size() > 10) {
      event.getChannel()
          .sendMessage(
              "***Too many users found for input " + messageContent.getArg(0).get() + ". Please be more specific.***")
          .queue();
      return;
    }
    Optional<Duration> oDuration = messageContent.getArg(1).map(String::toUpperCase).map(durationString -> {
      if (durationString.contains("D")) {
        return durationString.replaceFirst("\\d+D", "P$0T");
      } else {
        return "PT" + durationString;
      }
    }).map(Duration::parse);
    String durationString = oDuration
        .map(d -> " for " + d.toString().substring(2).replaceAll("(\\d[HMS])(?!$)", "$1 ").toLowerCase()).orElse("");

    possibleMembers.stream().map(m -> new ChoiceMenu.MenuEntry(m.getUser().getAsTag(), m.getId()))
        .forEach(builder::addEntry);
    builder.setChoiceHandler(e -> guild().addRoleToMember(guild().getMemberById(e.getValue()),
        guild().getRoleById(config.getMuteConfig().getMuteRoleId())).queue(x -> {
          info("Muted member {}{}", jda.getUserById(e.getValue()).getAsTag(), durationString);
          event.getChannel()
              .sendMessage("***Muted member " + jda.getUserById(e.getValue()).getAsTag() + durationString + "***")
              .queue();
          connectionHelper.update("insert into mutedmembers (userid, muteduntil) VALUES (?,?)", e.getValue(),
              Instant.now().plus(oDuration.orElse(Duration.ofDays(100000l))).toString());
        }));
    builder.setTitle("Mute member");
    builder.setDescription("Choose a member to be muted" + durationString);

    ChoiceMenu menu = builder.build();
    activeMenus.put(menu.display(event.getChannel()), menu);
  }

  @Override
  protected boolean hasAccess(Member member) {
    return member.hasPermission(Permission.BAN_MEMBERS);
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

  private class Unmuter extends TimerTask {

    @Override
    public void run() {
      List<String> users = connectionHelper.getResults("select userid from mutedmembers where muteduntil<?",
          rs -> rs.getString("userid"), Instant.now().toString());
      users.forEach(u -> guild().removeRoleFromMember(u, guild().getRoleById(config.getMuteConfig().getMuteRoleId()))
          .queue(unused -> connectionHelper.update("delete from mutedmembers where userid=?", u)));
    }
  }

  @Override
  protected String shortInfoInternal() {
    return "Mute a member";
  }

  @Override
  protected String usageInternal() {
    return commandString("<SEARCHPHRASE> [TIME]");
  }

  @Override
  protected String descriptionInternal() {
    return "Mute a member on the server. You can input a SEARCHPHRASE and (if not too many results are returned) can then interactively choose the member to be muted. \n"
        + "Optionally you can unmute a user automatically after a certain amount of TIME";
  }

  @Override
  protected String examplesInternal() {
    return commandString("gittertest") + "\n" + "Searches for users matching gittertest and mutes them forever.\n"
        + commandString("testuser 7d1h5m3s") + "\n"
        + "Searches for users matching testuser and mutes them for 7 days, 1 hour, 5 minutes and 3 seconds.";
  }

}

// end of file
