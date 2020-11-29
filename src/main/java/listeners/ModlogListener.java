package listeners;

import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import config.containers.ServerConfig;
import helpers.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;

public class ModlogListener extends AbstractListener {

  public ModlogListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getModlogConfig());
    connectionHelper.update(
        "create table if not exists messagecache(id INTEGER PRIMARY KEY not null, userid varchar(255) not null, channelid varchar(255) not null, messageid varchar(255) not null, message text not null, tmstmp TEXT not null);");
  }

  @Override
  public void onUserUpdateName(UserUpdateNameEvent event) {
    super.onUserUpdateName(event);
  }

  @Override
  protected void guildMessageReceived(GuildMessageReceivedEvent event) {
    super.guildMessageReceived(event);
    try {
      connectionHelper.update(
          "INSERT INTO messagecache(userid, channelid, messageid, message, tmstmp) values (?,?,?,?,?)",
          event.getMember().getId(), event.getChannel().getId(), event.getMessageId(),
          event.getMessage().getContentDisplay(), Instant.now().toString());
    } catch (Exception e) {
      error("Couldn't get all from event. member: " + event.getMember() + " | channel: " + event.getChannel() + "\n "
          + e);
    }
  }

  @Override
  public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
    super.onGuildMessageUpdate(event);
    if (!event.getGuild().getId().equals(guild.getId())) {
      return;
    }
    if (Optional.ofNullable(event).map(GuildMessageUpdateEvent::getMember).map(Member::getUser).map(User::isBot)
        .orElse(false)) {
      return;
    }
    Optional<String> oldMessage = connectionHelper.getFirstResult(
        "select message from messagecache where messageid = ? and channelid=?", rs -> rs.getString("message"),
        event.getMessageId(), event.getChannel().getId());
    EmbedBuilder builder = getBuilder();
    setEmbedAuthor(builder, event.getMember());
    if (oldMessage.isPresent()) {
      builder.addField("Old Message", oldMessage.map(oM -> shortenMessage(oM)).get(), false);
    } else {
      builder.addField("Old Message", "Unknown", false);
    }
    builder.addField("New Message", event.getMessage().getContentDisplay(), false);
    builder.setDescription("Message edited in " + event.getChannel().getAsMention());
    builder.setColor(Color.decode("#117ea6"));
    builder.setFooter("User ID: " + event.getMember().getId());
    guild().getTextChannelById(config.getModlogConfig().getChannelId()).sendMessage(builder.build()).queue();
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        connectionHelper.update("Update messagecache set message=? where messageid=? and channelid=?",
            event.getMessage().getContentDisplay(), event.getMessageId(), event.getChannel().getId());
      }
    }, 500l); //this is a really dirty hack, but in order to get editable suggestions we need to avoid a race condition here
  }

  private static String shortenMessage(String oM) {
    return (oM.length() > 1020) ? oM.substring(0, 1020) + "…" : oM;
  }

  @Override
  public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
    super.onGuildMessageDelete(event);
    if (!event.getGuild().getId().equals(guild.getId())) {
      return;
    }
    Optional<List<String>> oldMessage = connectionHelper.getFirstResult(
        "select message, userid from messagecache where messageid = ? and channelid=?",
        rs -> Stream.of(rs.getString("message"), rs.getString("userid")).collect(Collectors.toList()),
        event.getMessageId(), event.getChannel().getId());
    EmbedBuilder builder = getBuilder();
    if (oldMessage.isPresent()) {
      builder.addField("Deleted Message", shortenMessage(oldMessage.get().get(0)), false);
      setEmbedAuthor(builder, guild().getMemberById(oldMessage.get().get(1)));
    } else {
      builder.addField("Deleted Message", "Unknown", false);
    }
    builder.setDescription("Message deleted in " + event.getChannel().getAsMention());
    builder.setColor(Color.decode("#ff470f"));
    builder.setFooter(
        "Author: " + oldMessage.map(x -> x.get(1)).orElse("Unknown") + " | Message ID: " + event.getMessageId());
    guild().getTextChannelById(config.getModlogConfig().getChannelId()).sendMessage(builder.build()).queue();
    connectionHelper.update("delete from messagecache where messageid=? and channelid=?", event.getMessageId(),
        event.getChannel().getId());
  }

  @Override
  public void onGuildBan(GuildBanEvent event) {
    super.onGuildBan(event);
    if (!event.getGuild().getId().equals(guild.getId())) {
      return;
    }
    EmbedBuilder builder = getBuilder();
    setEmbedAuthor(builder, event.getUser());
    builder.setDescription("User banned");
    builder.addField("User", event.getUser().getAsTag(), false);
    builder.setColor(Color.decode("#ff470f"));
    builder.setFooter("User ID: " + event.getUser().getId());
    guild().getTextChannelById(config.getModlogConfig().getChannelId()).sendMessage(builder.build()).queue();
  }

  @Override
  public void onGuildMemberJoin(GuildMemberJoinEvent event) {
    super.onGuildMemberJoin(event);
    if (!event.getGuild().getId().equals(guild.getId())) {
      return;
    }
    EmbedBuilder builder = getBuilder();
    builder.setDescription("User Joined");
    setEmbedAuthor(builder, event.getMember());
    builder.addField("User", event.getUser().getAsTag(), false);
    Duration sinceCreated = Duration.between(event.getUser().getTimeCreated(), OffsetDateTime.now());
    if (sinceCreated.toDays() < 7) {
      builder.addField("New Account", "Created " + Utilities.formatDuration(sinceCreated) + " ago", false);
    }
    builder.setColor(Color.decode("#23d160"));
    builder.setFooter("User ID: " + event.getUser().getId());
    guild().getTextChannelById(config.getModlogConfig().getChannelId()).sendMessage(builder.build()).queue();
  }

  @Override
  public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
    super.onGuildMemberRemove(event);
    if (!event.getGuild().getId().equals(guild.getId())) {
      return;
    }
    EmbedBuilder builder = getBuilder();
    builder.setDescription("User Left");
    setEmbedAuthor(builder, event.getMember());
    builder.addField("User", event.getUser().getAsTag(), false);
    builder.setColor(Color.decode("#ff470f"));
    builder.setFooter("User ID: " + event.getUser().getId());
    guild().getTextChannelById(config.getModlogConfig().getChannelId()).sendMessage(builder.build()).queue();
  }

  @Override
  public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
    super.onGuildMemberRoleAdd(event);
    if (!event.getGuild().getId().equals(guild.getId())) {
      return;
    }
    EmbedBuilder builder = getBuilder();
    builder.setDescription("User Roles added");
    setEmbedAuthor(builder, event.getMember());
    builder.addField("User", event.getUser().getAsTag(), false);
    builder.addField("Roles", event.getRoles().stream().map(Role::getName).collect(Collectors.joining(", ")), false);
    builder.setColor(Color.decode("#117ea6"));
    builder.setFooter("User ID: " + event.getUser().getId());
    guild().getTextChannelById(config.getModlogConfig().getChannelId()).sendMessage(builder.build()).queue();
  }

  @Override
  public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
    super.onGuildMemberRoleRemove(event);
    if (!event.getGuild().getId().equals(guild.getId())) {
      return;
    }
    EmbedBuilder builder = getBuilder();
    builder.setDescription("User Roles removed");
    setEmbedAuthor(builder, event.getMember());
    builder.addField("User", event.getUser().getAsTag(), false);
    builder.addField("Roles", event.getRoles().stream().map(Role::getName).collect(Collectors.joining(", ")), false);
    builder.setColor(Color.decode("#117ea6"));
    builder.setFooter("User ID: " + event.getUser().getId());
    guild().getTextChannelById(config.getModlogConfig().getChannelId()).sendMessage(builder.build()).queue();
  }

  @Override
  public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
    super.onGuildMemberUpdateNickname(event);
    if (!event.getGuild().getId().equals(guild.getId())) {
      return;
    }
    EmbedBuilder builder = getBuilder();
    builder.setDescription("User Roles removed");
    setEmbedAuthor(builder, event.getMember());
    builder.addField("User", event.getUser().getAsTag(), false);
    builder.addField("Old Nickname", Optional.ofNullable(event.getOldNickname()).orElse("None"), false);
    builder.addField("New Nickname", Optional.ofNullable(event.getNewNickname()).orElse("None"), false);
    builder.setColor(Color.decode("#117ea6"));
    builder.setFooter("User ID: " + event.getUser().getId());
    guild().getTextChannelById(config.getModlogConfig().getChannelId()).sendMessage(builder.build()).queue();
  }

  private static EmbedBuilder getBuilder() {
    EmbedBuilder builder = new EmbedBuilder();
    builder.setTimestamp(Instant.now());
    builder.setFooter("Test");
    return builder;
  }

}
