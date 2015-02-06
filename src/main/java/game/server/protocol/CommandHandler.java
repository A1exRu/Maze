package game.server.protocol;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.UUID;

public interface CommandHandler {

    void handle(SocketAddress address, ByteBuffer buff, UUID sessionUuid);
    
}
