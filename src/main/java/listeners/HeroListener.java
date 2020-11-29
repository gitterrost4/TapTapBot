package listeners;

import java.util.Optional;

import config.containers.ServerConfig;
import containers.CommandMessage;
import containers.Hero;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HeroListener extends AbstractHeroListener {

  public HeroListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getHeroConfig(), "hero", "\\|", config.getHeroConfig().getDatabaseFileName());
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    switch (messageContent.getArg(0).orElseThrow(() -> new IllegalArgumentException("need at least one argument"))) {
    case "add":
      if (!config.getHeroConfig().getEditAllowed()) {
        return;
      }
      if (event.getMember().getRoles().stream()
          .allMatch(role -> role.compareTo(guild().getRolesByName(config.getHeroConfig().getMinimumEditRole(), true)
              .stream().findFirst().orElseThrow(() -> new IllegalStateException("role not found"))) < 0)) {
        event.getChannel().sendMessage("You don't have permission to add a hero").queue();
        return;
      }
      connectionHelper.update(
          "insert into hero (name, emoji, imageurl, maxstar, faction, career, skill1name,skill1desc, skill2name,skill2desc, skill3name, skill3desc, skill4name, skill4desc, maxhp, attack, speed, defense,uppullrate) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
          messageContent.getArgOrThrow(1), messageContent.getArgOrThrow(2), messageContent.getArgOrThrow(3),
          messageContent.getArgOrThrow(4), messageContent.getArgOrThrow(5), messageContent.getArgOrThrow(6),
          messageContent.getArgOrThrow(7), messageContent.getArgOrThrow(8), messageContent.getArgOrThrow(9),
          messageContent.getArgOrThrow(10), messageContent.getArgOrThrow(11), messageContent.getArgOrThrow(12),
          messageContent.getArgOrThrow(13), messageContent.getArgOrThrow(14), messageContent.getArgOrThrow(15),
          messageContent.getArgOrThrow(16), messageContent.getArgOrThrow(17), messageContent.getArgOrThrow(18),
          messageContent.getArg(19).map(x -> !x.isEmpty()).orElse(null));
      break;
    case "delete":
      if (!config.getHeroConfig().getEditAllowed()) {
        return;
      }
      if (event.getMember().getRoles().stream()
          .allMatch(role -> role.compareTo(guild().getRolesByName(config.getHeroConfig().getMinimumEditRole(), true)
              .stream().findFirst().orElseThrow(() -> new IllegalStateException("role not found"))) < 0)) {
        event.getChannel().sendMessage("You don't have permission to delete a hero").queue();
        return;
      }
      connectionHelper.update("delete from hero where lower(name)=?", messageContent.getArgOrThrow(1).toLowerCase());
      break;
    default:
      String heroName = messageContent.getArg(0).get();
      Optional<Hero> oHero = getHeroFromName(heroName);
      if (!oHero.isPresent()) {
        event.getChannel().sendMessage("I couldn't find the hero " + heroName + ".").queue();
        return;
      }
      Hero hero = oHero.get();
      EmbedBuilder embedBuilder = new EmbedBuilder()
          .setAuthor(hero.name, null,
              guild().getEmotesByName(hero.emoji, true).stream().findAny().map(em -> em.getImageUrl()).orElse(null))
          .addField("Name", hero.name, false).addField("Maximum Star Level", hero.maxStar.toString(), false)
          .addField("Faction", hero.faction, false).addField("Class", hero.career, false)
          .addField("HP", hero.maxHp.toString(), true).addField("Attack", hero.attack.toString(), true)
          .addField("Speed", hero.speed.toString(), true).addField("Defense", hero.defense.toString(), true);
      if (hero.skill1Name != null) {
        embedBuilder.addField(hero.skill1Name, hero.skill1Desc, false);
      }
      if (hero.skill2Name != null) {
        embedBuilder.addField(hero.skill2Name, hero.skill2Desc, false);
      }
      if (hero.skill3Name != null) {
        embedBuilder.addField(hero.skill3Name, hero.skill3Desc, false);
      }
      if (hero.skill4Name != null) {
        embedBuilder.addField(hero.skill4Name, hero.skill4Desc, false);
      }
      MessageEmbed embed = embedBuilder.addField("Pulls for filled UP-bar (1st/2nd/3rd+ pull)",
          Optional.ofNullable(hero.upPullRate).filter(upr -> upr > 0)
              .map(upr -> String.format("%d/%.0f/%d", upr, upr * 1.5, upr * 2)).orElse("?"),
          true).build();
      event.getChannel().sendMessage(embed).queue();
    }
  }

  @Override
  protected String shortInfoInternal() {
    return "Get information about a hero";
  }

  @Override
  protected String usageInternal() {
    return commandString("<HERO>")+"\n";
  }

  @Override
  protected String descriptionInternal() {
    return "Displays information such as stats and skills of the required HERO. The hero name must be written correctly in order for it to work. The corresponding Emote may also work (this is not available for all heroes)";
  }

  @Override
  protected String examplesInternal() {
    return commandString("phoenix")+"\n" + "Displays information for Phoenix.";
  }

}
