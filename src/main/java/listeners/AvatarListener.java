package listeners;

import config.containers.ServerConfigImpl;
import de.gitterrost4.botlib.containers.CommandMessage;
import de.gitterrost4.botlib.listeners.AbstractMessageListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class AvatarListener extends AbstractMessageListener<ServerConfigImpl> {

  public AvatarListener(JDA jda, Guild guild, ServerConfigImpl config) {
    super(jda, guild, config, config.getAvatarConfig(), "avatar");
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    Member member = getMemberFromSearchString(messageContent.getArg(0), ()->event.getMember());
    event.getChannel().sendMessage(setEmbedAuthor(new EmbedBuilder(), member).setTitle("Avatar").setImage(member.getUser().getEffectiveAvatarUrl()+"?size=256").build())
    .queue();
  }

  @Override
  protected String shortInfoInternal() {
    return "Display the avatar of a user";
  }

  @Override
  protected String usageInternal() {
    return commandString("[USER]");
  }

  @Override
  protected String descriptionInternal() {
    return "Display the (full-size) avatar of the given USER. The bot will guess the best match for the given USER string. If USER is not given, display your own avatar.";
  }

  @Override
  protected String examplesInternal() {
    return commandString("")+"\n"
        + "Display your own avatar.\n"
        + commandString("itterro")+"\n"
        + "Display the avatar of a user whose name contains \"itterro\"";
  }
  
  

}
