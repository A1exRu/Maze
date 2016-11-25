package game.server.protocol;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import game.server.udp.Transmitter;

public class AckHandler implements CommandHandler {

    @Autowired
    private Transmitter transmitter;

    @Override
    public boolean isAuthRequired() {
        return true;
    }

    @Override
    public void handle(SocketAddress address, ByteBuffer buff, UUID sessionId) {
        long packetId = buff.getLong();
        int num = buff.getInt();
        transmitter.ack(sessionId, packetId, num);
    }
}
