import java.time.Instant;

// $Id $
// (C) cantamen/Paul Kramer 2019

/** 
 * TODO documentation
 *
 * @author (C) cantamen/Paul Kramer 2019
 * @version $Id $
 */
public class Suggestion {
  public final String userId;
  public final Instant timestamp;
  public Suggestion(String userId) {
    super();
    this.userId=userId;
    this.timestamp=Instant.now();
  }
  
}


// end of file
