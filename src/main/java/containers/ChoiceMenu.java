// $Id $
// (C) cantamen/Paul Kramer 2020
package containers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import helpers.Emoji;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

/** 
 * TODO documentation
 *
 */
public class ChoiceMenu {
  private final String title;
  private final String description;
  private final List<MenuEntry> entries;
  private Message message;
  private final Consumer<MenuEntry> choiceHandler;
  private int selected = 0;

  private ChoiceMenu(String title, String description, List<MenuEntry> entries, Consumer<MenuEntry> handler) {
    super();
    this.entries=entries;
    this.choiceHandler = handler;
    this.description = description;
    this.title = title;
  }

  public static class MenuEntry{
    private final String display;
    private final String value;
    public MenuEntry(String display, String value) {
      super();
      this.display=display;
      this.value=value;
    }
    public String getDisplay() {
      return display;
    }
    public String getValue() {
      return value;
    }
  }
  
  public String display(MessageChannel channel) {
    message = channel.sendMessage(buildEmbed().build()).complete();
    message.addReaction(Emoji.ARROW_UP_SMALL.asRepresentation()).queue();
    message.addReaction(Emoji.ARROW_DOWN_SMALL.asRepresentation()).queue();
    message.addReaction(Emoji.WHITE_CHECK_MARK.asRepresentation()).queue();
    message.addReaction(Emoji.NEGATIVE_SQUARED_CROSS_MARK.asRepresentation()).queue();
    return message.getId();
  }

  private EmbedBuilder buildEmbed() {
    EmbedBuilder builder = new EmbedBuilder();
    builder.addField("Choices",IntStream.range(0,entries.size()).mapToObj(i->((i==selected)?Emoji.ARROW_RIGHT.asString():Emoji.BLACK_LARGE_SQUARE.asString())+" "+entries.get(i).getDisplay()).collect(Collectors.joining("\n")),false);
    builder.addField("Selected option", entries.get(selected).getDisplay(),false);
    builder.setTitle(title);
    builder.setDescription(description);
    return builder;
  }

  public void update() {
    message.editMessage(buildEmbed().build()).queue();
  }

  public boolean handleReaction(MessageReactionAddEvent event) {
    if(event.getReactionEmote().getEmoji().equals(Emoji.ARROW_UP_SMALL.asString())) {
      event.getChannel().retrieveMessageById(event.getMessageId()).queue(message->message.removeReaction(event.getReactionEmote().getEmoji(),event.getUser()).queue());
      if(selected == 0) {
        selected = entries.size()-1;
      } else {
        selected--;
      }
      update();
      return false;
    }
    if(event.getReactionEmote().getEmoji().equals(Emoji.ARROW_DOWN_SMALL.asString())) {
      event.getChannel().retrieveMessageById(event.getMessageId()).queue(message->message.removeReaction(event.getReactionEmote().getEmoji(),event.getUser()).queue());
      if(selected == entries.size()-1) {
        selected = 0;
      } else {
        selected++;
      }
      update();
      return false;
    }
    if(event.getReactionEmote().getEmoji().equals(Emoji.WHITE_CHECK_MARK.asString())) {
      choiceHandler.accept(entries.get(selected));
      delete();
      return true;
    }
    if(event.getReactionEmote().getEmoji().equals(Emoji.NEGATIVE_SQUARED_CROSS_MARK.asString())) {
      delete();
      return true;
    }
    return false;
  }

  private void delete() {
    message.delete().queue();
  }
  
  public static ChoiceMenuBuilder builder() {
    return new ChoiceMenuBuilder();
  }
  
  public static class ChoiceMenuBuilder{
    List<MenuEntry> entries = new ArrayList<>();
    String title = "";
    String description = "";
    Consumer<MenuEntry> choiceHandler;
    
    private ChoiceMenuBuilder() {
      //nothing  
    }
    
    public ChoiceMenuBuilder addEntry(MenuEntry entry) {
      entries.add(entry);
      return this;
    }
    
    public ChoiceMenuBuilder setChoiceHandler(Consumer<MenuEntry> handler) {
      this.choiceHandler = handler;
      return this;
    }
    
    public ChoiceMenuBuilder setTitle(String title) {
      this.title=title;
      return this;
    }
    
    public ChoiceMenuBuilder setDescription(String description) {
      this.description=description;
      return this;
    }
    
    public ChoiceMenu build() {
      if(choiceHandler==null) {
        throw new IllegalStateException("Uninitialized handler for menu");
      }
      return new ChoiceMenu(title,description,entries,choiceHandler);
    }
    
  }

}


// end of file
