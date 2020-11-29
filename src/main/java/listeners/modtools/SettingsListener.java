package listeners.modtools;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import config.Config;
import config.containers.ServerConfig;
import containers.CommandMessage;
import listeners.AbstractMessageListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

//doesn't work currently needs rewriting
@SuppressWarnings("all")
public class SettingsListener extends AbstractMessageListener {

  public SettingsListener(JDA jda, Guild guild, ServerConfig config) {
    super(jda, guild, config, null, "settings");
  }

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
//    if (!guild().getMember(event.getAuthor()).hasPermission(Permission.ADMINISTRATOR)) {
//      event.getChannel().sendMessage("***You don't have the right to change bot settings!***").queue();
//      return;
//    }
//
//    switch (messageContent.getArg(0).orElse(null)) {
//    case "set":
//      try {
//        messageContent.getArg(1)
//            .ifPresent(setting -> messageContent.getArg(2).ifPresent(value -> Config.writeValue(setting, value)));
//      } catch (Exception e) {
//        event.getChannel().sendMessage("Something went wrong when writing the config file").queue();
//        throw e;
//      }
//      // fallthrough
//    case "get":
//      String string = messageContent.getArg(1)
//          .map(setting -> Stream.of((Entry<String, String>) (new SimpleEntry<>(setting, Config.get(setting)))))
//          .orElse(Config.getAll().entrySet().stream()).filter(x -> !x.getKey().equals("bot.token"))
//          .map(x -> x.getKey() + "=" + x.getValue()).sorted().collect(Collectors.joining("\n"));
//      Optional.of(string).filter(s -> !s.isEmpty()).ifPresent(
//          s -> event.getChannel().sendMessage("`" + s.substring(0, Math.min(1900, string.length())) + "`").queue());
//    }
  }
}
