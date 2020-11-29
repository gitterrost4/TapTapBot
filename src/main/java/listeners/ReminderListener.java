package listeners;

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
import containers.CommandMessage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ReminderListener extends AbstractMessageListener {

  public ReminderListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getReminderConfig(), "remind");
    connectionHelper.update(
        "create table if not exists reminder (id INTEGER PRIMARY KEY not null, memberid text not null, channelid text, remindertext text not null, referencedmessageurl text, remindertime text);");
    Timer t = new Timer();
    t.scheduleAtFixedRate(new Reminder(), 0, 5000);
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    String timeString = messageContent.getArgOrThrow(0);
    Instant reminderTime = convertTimeString(timeString).orElseThrow(()->new IllegalStateException("could not parse time"));
    String memberId = event.getMember().getId();
    int i = 1;
    String channelId = null;
    if(!messageContent.getArgOrThrow(i).equals("--dm")) {
      channelId = event.getChannel().getId();
    } else {
      i++;
    }
    String reminderText = messageContent.getArgOrThrow(i,true);
    String referencedMessageUrl = null;
    if(reminderText.equals("--prev")) {
      Message message = event.getTextChannel().getHistory().retrievePast(2).complete().get(1);
      reminderText = message.getContentRaw();
      referencedMessageUrl = message.getJumpUrl(); 
    } else if (reminderText.matches("[0-9]+")) {
      Message message = event.getTextChannel().retrieveMessageById(reminderText).complete();
      reminderText = message.getContentRaw();
      referencedMessageUrl = message.getJumpUrl();
    } 
    connectionHelper.update("insert into reminder (memberid, channelid, remindertext, referencedmessageurl, remindertime) VALUES (?,?,?,?,?)", memberId, channelId, reminderText, referencedMessageUrl, reminderTime);
    event.getChannel().sendMessage("I will be reminding you "+(channelId==null?"via DM ":"")+"at "+reminderTime.toString()).queue();
  }
  
  private static Optional<Instant> convertTimeString(String timeString) {
    try {
      return Optional.of(Instant.parse(timeString));
    } catch (@SuppressWarnings("unused")Exception e) {
    }
    try {
      return Optional.of(OffsetDateTime.parse(timeString).toInstant());
    } catch (@SuppressWarnings("unused")Exception e) {
    }
    try {
      return Optional.of(timeString).map(String::toUpperCase).map(durationString -> {
        if (durationString.contains("D")) {
          return durationString.replaceFirst("\\d+D", "P$0T");
        } else {
          return "PT" + durationString;
        }
      }).map(Duration::parse)
      .map(d->Instant.now().plus(d));
    } catch (@SuppressWarnings("unused")Exception e) {
    }
    return Optional.empty();
  }
  
  private class Reminder extends TimerTask {
    @Override
    public void run() {
      List<List<String>> reminders = connectionHelper.getResults("select id, memberid, channelid, remindertext, referencedmessageurl from reminder where reminderTime<?",
          rs -> Stream.of(rs.getString("id"),rs.getString("memberid"), rs.getString("channelid"), rs.getString("remindertext"), rs.getString("referencedmessageurl")).collect(Collectors.toList()), Instant.now().toString());
      reminders.stream().forEach(reminder->{
        System.err.println(reminder);
        Member member = guild().getMemberById(reminder.get(1));
        MessageChannel channel = Optional.ofNullable(reminder.get(2)).map(channelId->guild().getTextChannelById(channelId)).map((c->(MessageChannel)c)).orElseGet(()-> member.getUser().openPrivateChannel().complete());
        channel.sendMessage("This is a reminder for "+member.getAsMention()+"\n"
            + ">>> "+reminder.get(3)+Optional.ofNullable(reminder.get(4)).map(s->"\n"+s).orElse("")).queue(unused->{
              // only delete the database entry if the message was actually sent
              connectionHelper.update("delete from reminder where id=?", reminder.get(0));
            });
      });
    }
  }

  @Override
  protected String shortInfoInternal() {
    return "Let the bot remind you about something";
  }

  @Override
  protected String usageInternal() {
    return commandString("<DATE> [--dm] [--prev] [REMINDER_TEXT]");
  }

  @Override
  protected String descriptionInternal() {
    return "Let the bot remind you at <DATE> about something. You can either give the --prev flag, which will remind you about the last message in the channel or you give a custom REMINDER_TEXT.\n"
        + "DATE can be either an ISO 8601 string or it can be a relative time (7d5h3m1s would be seven days, five hours, three minutes and one second from now)\n"
        + "If you give the --dm flag, the bot will remind you in DMs instead of the channel where the command was issued."; 
  }

  @Override
  protected String examplesInternal() {
    return commandString("7d hello world")+"\n"
        + "Remind yourself about \"hello world\" in seven days from now\n"
        + commandString("2020-10-21T16:55:44.960Z --prev")+"\n"
        + "Remind yourself about the previous message on the 21st of October 2020 at 16:55 (UTC)";
  }
  
  

 

}
