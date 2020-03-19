package listeners;

import java.util.Optional;

import config.Config;
import containers.CommandMessage;
import database.ConnectionHelper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class GiftCodeListener extends AbstractMessageListener {

  private static final String CURRENTLY_NO_CODES = "Currently no codes";

  public GiftCodeListener(JDA jda) {
    super(jda, "giftcode");
    ConnectionHelper.update(
        "create table if not exists giftcodes(id INTEGER PRIMARY KEY not null, giftcode varchar(255), gemreward INTEGER not null, channelid TEXT null, active INTEGER not null default 1);");
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    if (!guild().getMember(event.getAuthor()).hasPermission(Permission.BAN_MEMBERS)) {
      return;
    }
    if (messageContent.getArg(0).filter(x -> x.equals("add")).isPresent()) {
      String code = messageContent.getArg(1).orElseThrow(() -> new IllegalArgumentException("no code given"));
      String gemReward = messageContent.getArg(2).orElseThrow(() -> new IllegalArgumentException("no gemReward given"));
      guild().getCategoryById(Config.get("giftcodes.categoryId")).createVoiceChannel(code + " (" + gemReward + " Gems)")
          .queue(vc -> ConnectionHelper.update("INSERT INTO giftcodes (giftcode,gemreward,channelid) VALUES (?,?,?)",
              code, gemReward, vc.getId()));
      guild().getCategoryById(Config.get("giftcodes.categoryId")).getVoiceChannels().stream()
          .filter(c -> c.getName().equals(CURRENTLY_NO_CODES)).forEach(vc -> vc.delete().queue());
    } else if (messageContent.getArg(0).filter(x -> x.equals("delete")).isPresent()) {
      String code = messageContent.getArg(1).orElseThrow(() -> new IllegalArgumentException("no code given"));
      Optional<String> oChannelId = ConnectionHelper.getFirstResult(
          "SELECT channelid from giftcodes where giftcode=? COLLATE NOCASE", rs -> rs.getString("channelid"), code);
      oChannelId.ifPresent(channelId -> {
        guild().getVoiceChannelById(channelId).delete().complete();
        ConnectionHelper.update("DELETE FROM giftcodes where channelid=?", channelId);
      });
      Integer codeCount = ConnectionHelper
          .getFirstResult("SELECT count(*) as codecount from giftcodes", rs -> rs.getInt("codecount")).get();
      // this will always yield a result
      if (codeCount == 0) {
        guild().getCategoryById(Config.get("giftcodes.categoryId")).createVoiceChannel(CURRENTLY_NO_CODES).queue();
      }
    }
  }
}
