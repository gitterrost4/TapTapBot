package containers;

/**
 * Flat container class for heroes
 * 
 * @author gitterrost4
 */
public class Hero {

  public final String name;
  public final String emoji;
  public final String imageUrl;
  public final String skill1;
  public final String skill2;
  public final String skill3;
  public final String skill4;

  public Hero(String name, String emoji, String imageUrl, String skill1, String skill2, String skill3, String skill4) {
    super();
    this.name = name;
    this.emoji = emoji;
    this.imageUrl = imageUrl;
    this.skill1 = skill1;
    this.skill2 = skill2;
    this.skill3 = skill3;
    this.skill4 = skill4;
  }

}
