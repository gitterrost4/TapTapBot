package containers;
import java.time.Instant;

// $Id $
// (C) cantamen/Paul Kramer 2019

/** 
 * container class for remembering which user made suggestions at which time point
 *
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
