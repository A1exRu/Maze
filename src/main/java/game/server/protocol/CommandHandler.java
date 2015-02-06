package game.server.protocol;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

public interface CommandHandler {

    void handle(SocketAddress address, ByteBuffer buff);
    
}
