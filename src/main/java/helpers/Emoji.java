// $Id $
// (C) cantamen/Paul Kramer 2020
package helpers;

/** 
 * TODO documentation
 *
 * @author (C) cantamen/Paul Kramer 2020
 * @version $Id $
 */
public enum Emoji {
  ROBOT("U+1F916"),
  APPLE("U+1F34E");
  private final String representation;
  private Emoji(String representation) {
    this.representation=representation;
  }
  
  public String asRepresentation() {
    return representation;
  }
  
  public String asString() {
    return new StringBuilder().appendCodePoint(Integer.parseInt(representation.substring(2),16)).toString();
  }
  
}


// end of file
