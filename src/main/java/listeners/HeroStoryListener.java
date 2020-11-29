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

public class HeroStoryListener extends AbstractHeroListener {

  
  
  
  public HeroStoryListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, config.getHeroConfig(), "story", "\\|", config.getHeroConfig().getDatabaseFileName());
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    String heroName = messageContent.getArg(0).get();
    Optional<Hero> oHero = getHeroFromName(heroName);
    if (!oHero.isPresent()) {
      event.getChannel().sendMessage("I couldn't find the hero " + heroName + ".").queue();
      return;
    }
    Hero hero = oHero.get();
    EmbedBuilder builder = new EmbedBuilder()
        .setAuthor(hero.name, null,
            guild().getEmotesByName(hero.emoji, true).stream().findAny().map(em -> em.getImageUrl()).orElse(null));
    MessageEmbed embed = addLongField(builder, "Story", hero.story).build();
    
    event.getChannel().sendMessage(embed).queue();
  }

}
