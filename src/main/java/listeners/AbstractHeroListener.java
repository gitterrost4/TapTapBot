package listeners;

import java.util.Optional;

import config.containers.ServerConfig;
import config.containers.modules.CommandModuleConfig;
import containers.Hero;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public abstract class AbstractHeroListener extends AbstractMessageListener{

  public AbstractHeroListener(JDA jda, Guild guild, ServerConfig config, CommandModuleConfig moduleConfig,
      String command, String commandSeparator, String databaseFileName) {
    super(jda, guild, config, moduleConfig, command, commandSeparator, databaseFileName);
    connectionHelper.update(
        "create table if not exists hero (id INTEGER PRIMARY KEY not null, name text not null UNIQUE, emoji text, imageurl text, maxstar integer, faction text, career text, skill1name text, skill1desc text, skill2name text, skill2desc text, skill3name text, skill3desc text, skill4name text, skill4desc text, maxhp integer, attack integer, speed integer, defense integer, uppullrate integer,story text);");
  }

  protected Optional<Hero> getHeroFromName(String heroName) {
    info("Looking for hero {}", heroName);
    return connectionHelper.getFirstResult(
        "select name, emoji,imageurl, maxstar,faction,career, skill1name,skill1desc,skill2name,skill2desc,skill3name,skill3desc,skill4name,skill4desc,maxhp,attack,speed,defense,uppullrate,story from hero where lower(name)=? or ? like \"%\"||emoji||\"%\"",
        rs -> new Hero(rs.getString("name"), rs.getString("emoji"), rs.getString("imageUrl"), rs.getInt("maxstar"),
            rs.getString("faction"), rs.getString("career"), rs.getString("skill1name"), rs.getString("skill1desc"),
            rs.getString("skill2name"), rs.getString("skill2desc"), rs.getString("skill3name"),
            rs.getString("skill3desc"), rs.getString("skill4name"), rs.getString("skill4desc"), rs.getInt("maxhp"),
            rs.getInt("attack"), rs.getInt("speed"), rs.getInt("defense"), rs.getInt("uppullrate"), rs.getString("story")),
        heroName.toLowerCase(), heroName);
  }
  
}
