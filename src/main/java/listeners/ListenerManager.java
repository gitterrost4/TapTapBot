package listeners;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.JDA;

public class ListenerManager {
  public ListenerManager(JDA jda) {
    super();
    this.jda = jda;
  }

  private JDA jda;
  
  private List<AbstractListener> listeners = new ArrayList<>();

  public List<AbstractListener> getListeners() {
    return listeners;
  }
  
  public void addEventListener(AbstractListener listener) {
    listeners.add(listener);
    jda.addEventListener(listener);
  }
}
