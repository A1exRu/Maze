package game.core;

import java.util.HashMap;
import java.util.Map;

public class Converters {
    
    private Map<Integer, FUpdate> converters = new HashMap<>();
    
    public void add(int type, FUpdate converter) {
        if (!converters.containsKey(type)) {
            converters.put(type, converter);
        }
    }
    
    public FUpdate get(int type) {
        return converters.get(type);
    }
    
    
    
}
