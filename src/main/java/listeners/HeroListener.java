package listeners;

import java.util.Optional;

import config.Config;
import containers.CommandMessage;
import containers.Hero;
import database.ConnectionHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HeroListener extends AbstractMessageListener {

  public HeroListener(JDA jda) {
    super(jda, "hero", "\\|");
    ConnectionHelper.update(
        "create table if not exists hero (id INTEGER PRIMARY KEY not null, name text not null UNIQUE, emoji text, imageurl text, maxstar integer, skill1name text, skill1desc text, skill2name text, skill2desc text, skill3name text, skill3desc text, skill4name text, skill4desc text, maxhp integer, attack integer, speed integer, defense integer, uppullrate integer);");
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    switch (messageContent.getArg(0).orElseThrow(() -> new IllegalArgumentException("need at least one argument"))) {
    case "add":
      if (event.getMember().getRoles().stream()
          .allMatch(role -> role.compareTo(guild().getRolesByName(Config.get("hero.minimumEditRole"), true).stream()
              .findFirst().orElseThrow(() -> new IllegalStateException("role not found"))) < 0)) {
        event.getChannel().sendMessage("You don't have permission to add a hero").queue();
        return;
      }
      ConnectionHelper.update(
          "insert into hero (name, emoji, imageurl, maxstar, skill1name,skill1desc, skill2name,skill2desc, skill3name, skill3desc, skill4name, skill4desc, maxhp, attack, speed, defense,uppullrate) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
          messageContent.getArgOrThrow(1), messageContent.getArgOrThrow(2), messageContent.getArgOrThrow(3),
          messageContent.getArgOrThrow(4), messageContent.getArgOrThrow(5), messageContent.getArgOrThrow(6),
          messageContent.getArgOrThrow(7), messageContent.getArgOrThrow(8), messageContent.getArgOrThrow(9),
          messageContent.getArgOrThrow(10), messageContent.getArgOrThrow(11), messageContent.getArgOrThrow(12),
          messageContent.getArgOrThrow(13), messageContent.getArgOrThrow(14), messageContent.getArgOrThrow(15), messageContent.getArgOrThrow(16),
          messageContent.getArg(17).map(x -> !x.isEmpty()).orElse(null));
      break;
    case "delete":
      if (event.getMember().getRoles().stream()
          .allMatch(role -> role.compareTo(guild().getRolesByName(Config.get("hero.minimumEditRole"), true).stream()
              .findFirst().orElseThrow(() -> new IllegalStateException("role not found"))) < 0)) {
        event.getChannel().sendMessage("You don't have permission to delete a hero").queue();
        return;
      }
      ConnectionHelper.update("delete from hero where lower(name)=?", messageContent.getArgOrThrow(1).toLowerCase());
      break;
    default:
      String heroName = messageContent.getArg(0).get();
      Optional<Hero> oHero = ConnectionHelper.getFirstResult(
          "select name, emoji,imageurl, maxstar, skill1name,skill1desc,skill2name,skill2desc,skill3name,skill3desc,skill4name,skill4desc,maxhp,attack,speed,defense,uppullrate from hero where lower(name)=?",
          rs -> new Hero(rs.getString("name"), rs.getString("emoji"), rs.getString("imageUrl"), rs.getInt("maxstar"),
              rs.getString("skill1name"), rs.getString("skill1desc"), rs.getString("skill2name"),
              rs.getString("skill2desc"), rs.getString("skill3name"), rs.getString("skill3desc"),
              rs.getString("skill4name"), rs.getString("skill4desc"), rs.getInt("maxhp"), rs.getInt("attack"),
              rs.getInt("speed"), rs.getInt("defense"), rs.getInt("uppullrate")),
          heroName.toLowerCase());
      if (!oHero.isPresent()) {
        event.getChannel().sendMessage("I couldn't find the hero " + heroName + ".").queue();
        return;
      }
      Hero hero = oHero.get();
      EmbedBuilder embedBuilder = new EmbedBuilder()
          .setAuthor(hero.name, null,
              guild().getEmotesByName(hero.emoji, true).stream().findAny().map(em -> em.getImageUrl())
                  .orElse(null))
          .addField("Name", hero.name, false)
          .addField("Maximum Star Level", hero.maxStar.toString(), false)
          .addField("HP", hero.maxHp.toString(), true)
          .addField("Attack", hero.attack.toString(), true).addField("Speed", hero.speed.toString(), true)
          .addField("Defense", hero.defense.toString(), true);
      if(hero.skill1Name!=null) {
        embedBuilder.addField(hero.skill1Name, hero.skill1Desc, false);
      }
      if(hero.skill2Name!=null) {
        embedBuilder.addField(hero.skill2Name, hero.skill2Desc, false);
      }
      if(hero.skill3Name!=null) {
        embedBuilder.addField(hero.skill3Name, hero.skill3Desc, false);
      }
      if(hero.skill4Name!=null) {
        embedBuilder.addField(hero.skill4Name, hero.skill4Desc, false);
      }
      MessageEmbed embed = embedBuilder
          .addField("Pulls for filled UP-bar (1st/2nd/3rd+ pull)",
              Optional.ofNullable(hero.upPullRate).filter(upr->upr>0)
                  .map(upr -> String.format("%d/%.0f/%d", upr, upr * 1.5, upr * 2)).orElse("?"),
              true)
          .build();
      event.getChannel()
          .sendMessage(
              embed)
          .queue();
    }
  }

}
