package game.server.protocol;

import game.server.udp.SessionsHolder;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.UUID;

public class AckHandler implements CommandHandler {

    private SessionsHolder sessions;

    public AckHandler(SessionsHolder sessions) {
        this.sessions = sessions;
    }
    
    @Override
    public boolean isAuthRequired() {
        return true;
    }

    @Override
    public void handle(SocketAddress address, ByteBuffer buff, UUID token) {
        long packetId = buff.getLong();
        int num = buff.getInt();
        sessions.ack(token, packetId, num);    
    }
}
