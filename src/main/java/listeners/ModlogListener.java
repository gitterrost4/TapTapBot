package listeners;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.Config;
import database.ConnectionHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;

public class ModlogListener extends AbstractListener {

  public ModlogListener(JDA jda) {
    super(jda);
    ConnectionHelper.update(
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
      ConnectionHelper.update(
          "INSERT INTO messagecache(userid, channelid, messageid, message, tmstmp) values (?,?,?,?,?)",
          event.getMember().getId(), event.getChannel().getId(), event.getMessageId(),
          event.getMessage().getContentDisplay(), Instant.now().toString());
    } catch (Exception e) {
      Logger logger = LoggerFactory.getLogger(ModlogListener.class);
      logger.error("Couldn't get all from event. member: " + event.getMember() + " | channel: " + event.getChannel()
          + "\n " + e);
      System.err.println("got error in recording message");
    }
  }

  @Override
  public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
    super.onGuildMessageUpdate(event);
    if (!event.getGuild().getId().equals(Config.get("bot.serverId"))) {
      return;
    }
    if (Optional.ofNullable(event).map(GuildMessageUpdateEvent::getMember).map(Member::getUser).map(User::isBot)
        .orElse(false)) {
      return;
    }
    Optional<String> oldMessage = ConnectionHelper.getFirstResult(
        "select message from messagecache where messageid = ? and channelid=?", rs -> rs.getString("message"),
        event.getMessageId(), event.getChannel().getId());
    EmbedBuilder builder = new EmbedBuilder();
    setEmbedAuthor(builder, event.getMember());
    if (oldMessage.isPresent()) {
      builder.addField("Old Message", oldMessage.get(), false);
    } else {
      builder.addField("Old Message", "Unknown", false);
    }
    builder.addField("New Message", event.getMessage().getContentDisplay(), false);
    builder.setDescription("Message edited in " + event.getChannel().getAsMention());
    jda.getGuildById(Config.get("bot.serverId")).getTextChannelById(Config.get("modlog.channelId"))
        .sendMessage(builder.build()).queue();
    ConnectionHelper.update("Update messagecache set message=? where messageid=? and channelid=?",
        event.getMessage().getContentDisplay(), event.getMessageId(), event.getChannel().getId());
  }

  @Override
  public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
    super.onGuildMessageDelete(event);
    if (!event.getGuild().getId().equals(Config.get("bot.serverId"))) {
      return;
    }
    Optional<List<String>> oldMessage = ConnectionHelper.getFirstResult(
        "select message, userid from messagecache where messageid = ? and channelid=?",
        rs -> Stream.of(rs.getString("message"), rs.getString("userid")).collect(Collectors.toList()),
        event.getMessageId(), event.getChannel().getId());
    EmbedBuilder builder = new EmbedBuilder();
    if (oldMessage.isPresent()) {
      builder.addField("Deleted Message", oldMessage.get().get(0), false);
      setEmbedAuthor(builder, jda.getGuildById(Config.get("bot.serverId")).getMemberById(oldMessage.get().get(1)));
    } else {
      builder.addField("Deleted Message", "Unknown", false);
    }
    builder.setDescription("Message deleted in " + event.getChannel().getAsMention());
    jda.getGuildById(Config.get("bot.serverId")).getTextChannelById(Config.get("modlog.channelId"))
        .sendMessage(builder.build()).queue();
    ConnectionHelper.update("delete from messagecache where messageid=? and channelid=?", event.getMessageId(),
        event.getChannel().getId());
  }

  @Override
  public void onGuildBan(GuildBanEvent event) {
    super.onGuildBan(event);
    if (!event.getGuild().getId().equals(Config.get("bot.serverId"))) {
      return;
    }
    EmbedBuilder builder = new EmbedBuilder();
    setEmbedAuthor(builder, event.getUser());
    builder.setDescription("User banned");
    builder.addField("User", event.getUser().getAsTag(), false);
    jda.getGuildById(Config.get("bot.serverId")).getTextChannelById(Config.get("modlog.channelId"))
        .sendMessage(builder.build()).queue();
  }

  @Override
  public void onGuildMemberJoin(GuildMemberJoinEvent event) {
    super.onGuildMemberJoin(event);
    if (!event.getGuild().getId().equals(Config.get("bot.serverId"))) {
      return;
    }
    EmbedBuilder builder = new EmbedBuilder();
    builder.setDescription("User Joined");
    setEmbedAuthor(builder, event.getMember());
    builder.addField("User", event.getUser().getAsTag(), false);
    jda.getGuildById(Config.get("bot.serverId")).getTextChannelById(Config.get("modlog.channelId"))
        .sendMessage(builder.build()).queue();
  }

  @Override
  public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
    super.onGuildMemberLeave(event);
    if (!event.getGuild().getId().equals(Config.get("bot.serverId"))) {
      return;
    }
    EmbedBuilder builder = new EmbedBuilder();
    builder.setDescription("User Left");
    setEmbedAuthor(builder, event.getMember());
    builder.addField("User", event.getUser().getAsTag(), false);
    jda.getGuildById(Config.get("bot.serverId")).getTextChannelById(Config.get("modlog.channelId"))
        .sendMessage(builder.build()).queue();
  }

  @Override
  public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
    super.onGuildMemberRoleAdd(event);
    if (!event.getGuild().getId().equals(Config.get("bot.serverId"))) {
      return;
    }
    EmbedBuilder builder = new EmbedBuilder();
    builder.setDescription("User Roles added");
    setEmbedAuthor(builder, event.getMember());
    builder.addField("User", event.getUser().getAsTag(), false);
    builder.addField("Roles", event.getRoles().stream().map(Role::getName).collect(Collectors.joining(", ")), false);
    jda.getGuildById(Config.get("bot.serverId")).getTextChannelById(Config.get("modlog.channelId"))
        .sendMessage(builder.build()).queue();
  }

  @Override
  public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
    super.onGuildMemberRoleRemove(event);
    if (!event.getGuild().getId().equals(Config.get("bot.serverId"))) {
      return;
    }
    EmbedBuilder builder = new EmbedBuilder();
    builder.setDescription("User Roles removed");
    setEmbedAuthor(builder, event.getMember());
    builder.addField("User", event.getUser().getAsTag(), false);
    builder.addField("Roles", event.getRoles().stream().map(Role::getName).collect(Collectors.joining(", ")), false);
    jda.getGuildById(Config.get("bot.serverId")).getTextChannelById(Config.get("modlog.channelId"))
        .sendMessage(builder.build()).queue();
  }

  @Override
  public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
    super.onGuildMemberUpdateNickname(event);
    if (!event.getGuild().getId().equals(Config.get("bot.serverId"))) {
      return;
    }
    EmbedBuilder builder = new EmbedBuilder();
    builder.setDescription("User Roles removed");
    setEmbedAuthor(builder, event.getMember());
    builder.addField("User", event.getUser().getAsTag(), false);
    builder.addField("Old Nickname", Optional.ofNullable(event.getOldNickname()).orElse("None"), false);
    builder.addField("New Nickname", Optional.ofNullable(event.getNewNickname()).orElse("None"), false);
    jda.getGuildById(Config.get("bot.serverId")).getTextChannelById(Config.get("modlog.channelId"))
        .sendMessage(builder.build()).queue();
  }

}
