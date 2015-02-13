package game.server.protocol;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.UUID;

public interface CommandHandler {

    public default boolean isAuthRequired(){
        return false; 
    }
    
    void handle(SocketAddress address, ByteBuffer buff, UUID sessionUuid);
    
}
