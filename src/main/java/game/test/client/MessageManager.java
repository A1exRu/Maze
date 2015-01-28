package game.test.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private static final Logger logger = LoggerFactory.getLogger(MessageManager.class);
    private Map<Integer, MessageHandler> handlers = new HashMap<>();
    
    public void onMessage(byte[] bytes) {
        if (bytes == null || bytes.length < 3) {
            logger.warn("Invalid message bytes");
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
