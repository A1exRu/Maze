package game.scan.service;

import game.core.event.Event;
import game.core.event.EventHandler;
import game.core.event.EventService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Alexander on 10.11.2014.
 */
@Service
public class EventServiceImpl implements EventService {

    private Map<Class, List<EventHandler>> eventHolder;

    public EventServiceImpl() {
        eventHolder = new HashMap<>();
    }

    @Override
    public void subscribe(EventHandler handler) {
        List<EventHandler> handlers = eventHolder.get(handler.getEventClass());
        if (handlers == null) {
            handlers = new ArrayList<>();
            eventHolder.put(handler.getEventClass(), handlers);
        }

        handlers.add(handler);
    }

    @Override
    public void fire(Event event) {
        List<EventHandler> handlers = eventHolder.get(event.getObjectClass());
        if (handlers == null) {
            return;
        }

        for (EventHandler handler : handlers) {
            handler.handle(event);
        }
    }
}
