package helpers;

import java.time.Duration;
import java.util.List;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Utilities {

  public static String formatDuration(Duration duration) {
    String dayString = duration.toDays() > 0 ? duration.toHours() + " day, " : "";
    Duration minusDays = duration.minusDays(duration.toDays());
    String hourString = duration.toHours() > 0 ? minusDays.toHours() + " hrs, " : "";
    Duration minusHours = minusDays.minusHours(minusDays.toHours());
    String minuteString = duration.toMinutes() > 0 ? minusHours.toMinutes() + " min, " : "";
    String secondString = minusHours.minusMinutes(minusHours.toMinutes()).getSeconds() + " sec";
    return dayString + hourString + minuteString + secondString;
  }

  public static void deleteMessages(TextChannel channel, List<Message> retrievedHistory) {
  //    retrievedHistory.add(event.getMessage());
      if (retrievedHistory.size() > 1) {
        channel.deleteMessages(retrievedHistory).queue();
      } else {
        retrievedHistory.forEach(msg -> msg.delete().queue());
      }
    }
}
