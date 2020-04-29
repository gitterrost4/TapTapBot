package helpers;

import java.time.Duration;

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
}
