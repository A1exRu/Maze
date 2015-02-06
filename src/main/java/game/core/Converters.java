package game.core;

import java.util.HashMap;
import java.util.Map;

public class Converters {
    
    private Map<Integer, FMarshaller> converters = new HashMap<>();
    
    public void add(int type, FMarshaller converter) {
        if (!converters.containsKey(type)) {
            converters.put(type, converter);
        }
    }
    
    public <T>FMarshaller<T> get(int type) {
        return converters.get(type);
    }
    
    
    
}
