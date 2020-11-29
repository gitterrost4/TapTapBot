package listeners;

import java.awt.Color;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import config.containers.ServerConfig;
import containers.CommandMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class WhoisListener extends AbstractMessageListener {

  public WhoisListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getWhoisConfig(), "whois");
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    Optional<String> userString = messageContent.getArg(0,true);
    Member member = getMemberFromSearchString(userString, () -> event.getMember());
    List<Member> sortedMemberList = guild().getMemberCache().applyStream(stream->stream.sorted((x,y)->x.getTimeJoined().compareTo(y.getTimeJoined())).collect(Collectors.toList()));
    event.getChannel().sendMessage(setEmbedAuthor(new EmbedBuilder(), member).setDescription(member.getAsMention())
        .addField("Joined", member.getTimeJoined().format(DateTimeFormatter.ofPattern("EEE, MMM dd, uuuu h:mm a")), true)
        .addField("Join Position", String.valueOf(IntStream.range(0, sortedMemberList.size()).filter(i->member.getId().equals(sortedMemberList.get(i).getId())).findAny().orElse(-1)),true)
        .addField("Registered", member.getUser().getTimeCreated().format(DateTimeFormatter.ofPattern("EEE, MMM dd, uuuu h:mm a")), true)
        .addField("Roles [" + member.getRoles().size() + "]",
            member.getRoles().stream().map(Role::getAsMention).collect(Collectors.joining(" ")), false)
        .setColor(Color.MAGENTA)
        .setThumbnail(member.getUser().getEffectiveAvatarUrl() + "?size=256").build()).queue();
  }

  @Override
  protected String shortInfoInternal() {
    return "Display stats of a user";
  }

  @Override
  protected String usageInternal() {
    return commandString("[USER]");
  }

  @Override
  protected String descriptionInternal() {
    return "Display several stats (such as joined and registered times) of the given USER. The bot will guess the best match for the given USER string. If USER is not given, display your own stats.";
  }

  @Override
  protected String examplesInternal() {
    return commandString("")+"\n"
        + "Display your own stats.\n"
        + commandString("itterro")+"\n"
        + "Display the stats of a user whose name contains \"itterro\"";
  }
}
