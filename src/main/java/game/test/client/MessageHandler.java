package game.test.client;

import java.nio.ByteBuffer;

public interface MessageHandler {
    
    int getCode();
    
    void handle(ByteBuffer response);
    
}
