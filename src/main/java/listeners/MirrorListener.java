package listeners;

import java.awt.Color;
import java.util.Optional;

import config.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class MirrorListener extends AbstractListener {

  public MirrorListener(JDA jda) {
    super(jda);
  }

  @Override
  protected void guildMessageReceived(GuildMessageReceivedEvent event) {
    super.guildMessageReceived(event);

    if (Config.get("mirror.excludedChannelIds").contains(event.getChannel().getId())) {
      return;
    }

    String mirrorServerId = Config.get("mirror.mirrorServerId");
    Guild mirrorGuild = jda.getGuildById(mirrorServerId);

    Optional<GuildChannel> oChannel = mirrorGuild.getChannels().stream()
        .filter(c -> c.getName().equals(event.getChannel().getName())).findFirst();
    GuildChannel mirrorGuildChannel = oChannel
        .orElseGet(() -> mirrorGuild.createCopyOfChannel(event.getChannel()).complete());
    TextChannel mirrorChannel = jda.getTextChannelById(mirrorGuildChannel.getId());
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setColor(Color.BLACK);
    embedBuilder.setAuthor(event.getMember().getEffectiveName(), null, event.getMember().getUser().getAvatarUrl());
    embedBuilder.setDescription(event.getMessage().getContentDisplay());
    embedBuilder
        .setImage(event.getMessage().getAttachments().stream().map(Attachment::getUrl).findFirst().orElse(null));
    mirrorGuild.modifyNickname(mirrorGuild.getSelfMember(), event.getMember().getEffectiveName())
        .queue(unused -> mirrorChannel.sendMessage(embedBuilder.build()).queue());
  }

}
