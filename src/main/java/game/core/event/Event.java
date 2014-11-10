package game.core.event;

/**
 * Created by Alexander on 10.11.2014.
 */
public class Event<T> {

    private T object;
    private Class objectClass;

    public Event(T object) {
        this.object = object;
        this.objectClass = object.getClass();
    }

    public T getObject() {
        return object;
    }

    public Class getObjectClass() {
        return objectClass;
    }
}
