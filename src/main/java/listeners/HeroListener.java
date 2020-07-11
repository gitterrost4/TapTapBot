package listeners;

import java.util.Optional;

import config.Config;
import containers.CommandMessage;
import containers.Hero;
import database.ConnectionHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HeroListener extends AbstractMessageListener {

  public HeroListener(JDA jda) {
    super(jda, "hero", "\\|");
    ConnectionHelper.update(
        "create table if not exists hero (id INTEGER PRIMARY KEY not null, name text, emoji text, imageurl text, skill1 text, skill2 text, skill3 text, skill4 text);");
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
          "insert into hero (name, emoji, imageurl, skill1, skill2, skill3, skill4) values (?,?,?,?,?,?,?)",
          messageContent.getArgOrThrow(1), messageContent.getArgOrThrow(2), messageContent.getArgOrThrow(3),
          messageContent.getArgOrThrow(4), messageContent.getArgOrThrow(5), messageContent.getArgOrThrow(6),
          messageContent.getArgOrThrow(7));
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
      Optional<Hero> oHero = ConnectionHelper
          .getFirstResult("select name, emoji,imageurl, skill1,skill2,skill3,skill4 from hero where lower(name)=?",
              rs -> new Hero(rs.getString("name"), rs.getString("emoji"), rs.getString("imageUrl"),
                  rs.getString("skill1"), rs.getString("skill2"), rs.getString("skill3"), rs.getString("skill4")),
              heroName.toLowerCase());
      if (!oHero.isPresent()) {
        event.getChannel().sendMessage("I couldn't find the hero " + heroName + ".").queue();
        return;
      }
      Hero hero = oHero.get();
      event.getChannel()
          .sendMessage(
              new EmbedBuilder()
                  .setAuthor(hero.name, null,
                      guild().getEmotesByName(hero.emoji, true).stream().findAny().map(em -> em.getImageUrl())
                          .orElse(null))
                  .addField("Name", hero.name, false).addField("Skill 1", hero.skill1, false)
                  .addField("Skill 2", hero.skill2, false).addField("Skill 3", hero.skill3, false)
                  .addField("Skill 4", hero.skill4, false).setImage(hero.imageUrl).build())
          .queue();
    }
  }

}
