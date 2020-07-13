package listeners;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import config.Config;
import database.ConnectionHelper;
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
    ConnectionHelper.update(
        "create table if not exists mirrors(id INTEGER PRIMARY KEY not null, channelid text not null, mirrorserverid text not null, mirrorchannelid TEXT not null);");
  }

  @Override
  protected void guildMessageReceived(GuildMessageReceivedEvent event) {
    super.guildMessageReceived(event);

    List<List<String>> mirrors = ConnectionHelper.getResults(
        "select mirrorserverid, mirrorchannelid from mirrors where channelid=?",
        rs -> Arrays.asList(rs.getString("mirrorserverid"), rs.getString("mirrorchannelid")),
        event.getChannel().getId());

    mirrors.stream().forEach(m -> {
      String mirrorServerId = m.get(0);
      Guild mirrorGuild = jda.getGuildById(mirrorServerId);

      TextChannel mirrorChannel = mirrorGuild.getTextChannelById(m.get(1));
      mirrorChannel.sendMessage(event.getMessage().getContentRaw()).queue();
    });
  }

}
