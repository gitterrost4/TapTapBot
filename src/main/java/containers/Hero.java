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
  public final String skill1Name;
  public final String skill1Desc;
  public final String skill2Name;
  public final String skill2Desc;
  public final String skill3Name;
  public final String skill3Desc;
  public final String skill4Name;
  public final String skill4Desc;
  public final Integer maxHp;
  public final Integer attack;
  public final Integer speed;
  public final Integer defense;
  public Hero(String name, String emoji, String imageUrl, String skill1Name, String skill1Desc, String skill2Name,
      String skill2Desc, String skill3Name, String skill3Desc, String skill4Name, String skill4Desc, int maxHp,
      int attack, int speed, int defense) {
    super();
    this.name = name;
    this.emoji = emoji;
    this.imageUrl = imageUrl;
    this.skill1Name = skill1Name;
    this.skill1Desc = skill1Desc;
    this.skill2Name = skill2Name;
    this.skill2Desc = skill2Desc;
    this.skill3Name = skill3Name;
    this.skill3Desc = skill3Desc;
    this.skill4Name = skill4Name;
    this.skill4Desc = skill4Desc;
    this.maxHp = maxHp;
    this.attack = attack;
    this.speed = speed;
    this.defense = defense;
  }

}
