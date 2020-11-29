package listeners;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import config.containers.ServerConfig;
import helpers.Catcher;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class MirrorListener extends AbstractListener {

  public MirrorListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getMirrorConfig());
    connectionHelper.update(
        "create table if not exists mirrors(id INTEGER PRIMARY KEY not null, channelid text not null, mirrorserverid text not null, mirrorchannelid TEXT not null);");
  }

  @Override
  protected void guildMessageReceived(GuildMessageReceivedEvent event) {
    super.guildMessageReceived(event);

    List<List<String>> mirrors = connectionHelper.getResults(
        "select mirrorserverid, mirrorchannelid from mirrors where channelid=?",
        rs -> Arrays.asList(rs.getString("mirrorserverid"), rs.getString("mirrorchannelid")),
        event.getChannel().getId());

    mirrors.stream().forEach(m -> {
      String mirrorServerId = m.get(0);
      Guild mirrorGuild = jda.getGuildById(mirrorServerId);

      TextChannel mirrorChannel = mirrorGuild.getTextChannelById(m.get(1));
      MessageAction messageAction = mirrorChannel.sendMessage(Optional.ofNullable(event.getMessage().getContentRaw()).filter(x->!x.isEmpty()).orElse("."));
      event.getMessage().getAttachments().stream()
          .forEach(att -> messageAction.addFile(Catcher.wrap(()->att.downloadToFile().get())));
      messageAction.queue();
    });
  }

}
