package game.core.event;

/**
 * Created by Alexander on 10.11.2014.
 */
public interface EventService {

    void subscribe(EventHandler handler);

    void fire(Event event);
}
