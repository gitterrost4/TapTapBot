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
        "create table if not exists hero (id INTEGER PRIMARY KEY not null, name text, emoji text, skill1 text);");
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    switch (messageContent.getArg(0).orElseThrow(() -> new IllegalArgumentException("need at least one argument"))) {
    case "add":
      if (event.getMember().getRoles().stream()
          .allMatch(role -> role.compareTo(guild().getRolesByName(Config.get("hero.minimumEditRole"), true).stream()
              .findFirst().orElseThrow(() -> new IllegalStateException("role not found"))) < 0)) {
        event.getChannel().sendMessage("You don't have permission to add a hero").queue();
      }
      ConnectionHelper.update("insert into hero (name, emoji, skill1) values (?,?,?)", messageContent.getArgOrThrow(1),
          messageContent.getArgOrThrow(2), messageContent.getArgOrThrow(3));
      break;
    case "delete":
      if (event.getMember().getRoles().stream()
          .allMatch(role -> role.compareTo(guild().getRolesByName(Config.get("hero.minimumEditRole"), true).stream()
              .findFirst().orElseThrow(() -> new IllegalStateException("role not found"))) < 0)) {
        event.getChannel().sendMessage("You don't have permission to delete a hero").queue();
      }
      ConnectionHelper.update("delete from hero where lower(name)=?", messageContent.getArgOrThrow(1).toLowerCase());
      break;
    default:
      String heroName = messageContent.getArg(0).get();
      Optional<Hero> oHero = ConnectionHelper.getFirstResult("select name, emoji, skill1 from hero where lower(name)=?",
          rs -> new Hero(rs.getString("name"), rs.getString("emoji"), rs.getString("skill1")), heroName.toLowerCase());
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
                  .addField("Name", hero.name, false).addField("Skill 1", hero.skill1, false).build())
          .queue();
    }
  }

}
