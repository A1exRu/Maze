package game.bubble.update;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageManager {

    private static final Logger LOG = LoggerFactory.getLogger(MessageManager.class);
    private Map<Integer, MessageHandler> handlers = new HashMap<>();
    
    public void onMessage(byte[] bytes) {
        if (bytes == null || bytes.length < 3) {
            LOG.warn("[ERR-400]: Invalid message bytes");
            return;
        }
        
        ByteBuffer data = ByteBuffer.wrap(bytes);
        int handlerId = data.getInt();
        MessageHandler handler = handlers.get(handlerId);
        if (handler != null) {
            handler.handle(data);
        }
    }
    
    public void register(MessageHandler handler) {
        if (handlers.containsKey(handler.getCode())) {
            throw new IllegalStateException("Handler duplicate");
        }
        
        handlers.put(handler.getCode(), handler);
    }
    
}
