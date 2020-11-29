package listeners;

import java.util.Optional;

import config.containers.ServerConfig;
import containers.CommandMessage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class GiftCodeListener extends AbstractMessageListener {

  private static final String CURRENTLY_NO_CODES = "Currently no codes";

  public GiftCodeListener(JDA jda,Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getGiftCodeConfig(), "giftcode");
    connectionHelper.update(
        "create table if not exists giftcodes(id INTEGER PRIMARY KEY not null, giftcode varchar(255), gemreward INTEGER not null, channelid TEXT null, active INTEGER not null default 1);");
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    if (!hasAccess(guild().getMember(event.getAuthor()))) {
      return;
    }
    if (messageContent.getArg(0).filter(x -> x.equals("add")).isPresent()) {
      String code = messageContent.getArg(1).orElseThrow(() -> new IllegalArgumentException("no code given"));
      String gemReward = messageContent.getArg(2).orElseThrow(() -> new IllegalArgumentException("no gemReward given"));
      guild().getCategoryById(config.getGiftCodeConfig().getCategoryId()).createVoiceChannel(code + " (" + gemReward + " Gems)")
          .queue(vc -> connectionHelper.update("INSERT INTO giftcodes (giftcode,gemreward,channelid) VALUES (?,?,?)",
              code, gemReward, vc.getId()));
      guild().getCategoryById(config.getGiftCodeConfig().getCategoryId()).getVoiceChannels().stream()
          .filter(c -> c.getName().equals(CURRENTLY_NO_CODES)).forEach(vc -> vc.delete().queue());
    } else if (messageContent.getArg(0).filter(x -> x.equals("delete")).isPresent()) {
      String code = messageContent.getArg(1).orElseThrow(() -> new IllegalArgumentException("no code given"));
      Optional<String> oChannelId = connectionHelper.getFirstResult(
          "SELECT channelid from giftcodes where giftcode=? COLLATE NOCASE", rs -> rs.getString("channelid"), code);
      oChannelId.ifPresent(channelId -> {
        guild().getVoiceChannelById(channelId).delete().complete();
        connectionHelper.update("DELETE FROM giftcodes where channelid=?", channelId);
      });
      Integer codeCount = connectionHelper
          .getFirstResult("SELECT count(*) as codecount from giftcodes", rs -> rs.getInt("codecount")).get();
      // this will always yield a result
      if (codeCount == 0) {
        guild().getCategoryById(config.getGiftCodeConfig().getCategoryId()).createVoiceChannel(CURRENTLY_NO_CODES).queue();
      }
    }
  }

  @Override
  protected String shortInfoInternal() {
    return "Add or delete giftcodes on the sidebar";
  }

  @Override
  protected String usageInternal() {
    // TODO Auto-generated method stub
    return commandString("add <GIFTCODE> <GEM_AMOUNT>")+"\n"
        + commandString("delete <GIFTCODE>");
  }

  @Override
  protected String descriptionInternal() {
    return "Add a GIFTCODE to the sidebar by giving a GEM_AMOUNT that's granted by the giftcode. This ignores other possible rewards.";
  }

  @Override
  protected String examplesInternal() {
    return commandString("add ABC123 700")+"\n"
        + "Adds the giftcode ABC123 to the side bar with 700 gems as the reward.\n"
        + commandString("delete ABC123")+"\n"
            + "Deletes the giftcode from the sidebar.";
  }

  @Override
  protected boolean hasAccess(Member member) {
    return member.hasPermission(Permission.BAN_MEMBERS);
  }
  
}
