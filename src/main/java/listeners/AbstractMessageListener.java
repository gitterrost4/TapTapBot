// $Id $
// (C) cantamen/Paul Kramer 2019
package listeners;

import config.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/** 
 * TODO documentation
 *
 * @author (C) cantamen/Paul Kramer 2019
 * @version $Id $
 */
public abstract class AbstractMessageListener extends ListenerAdapter{

  protected static String PREFIX=Config.get("bot.prefix");
  protected final JDA jda;
  
  public AbstractMessageListener(JDA jda) {
    super();
    this.jda=jda;
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    super.onMessageReceived(event);
    String messageContent=event.getMessage().getContentRaw();

    if (event.getAuthor().isBot()) {
      return;
    }
    
    if(!event.getGuild().getId().equals(Config.get("bot.serverId"))) {
      return;
    }

    if (messageContent.toLowerCase().startsWith(PREFIX + getCommand()+" ")) {
      execute(event);
    }
  }
  
  protected abstract String getCommand();
  
  protected abstract void execute(MessageReceivedEvent event);

}


// end of file
