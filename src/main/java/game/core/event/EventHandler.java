package game.core.event;

/**
 * Created by Alexander on 10.11.2014.
 */
public interface EventHandler<T> {

    Class<T> getEventClass();

    void handle(Event<T> event);


}
