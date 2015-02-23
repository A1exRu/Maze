package game.server.protocol;

import game.server.udp.Transmitter;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.UUID;

public class AckHandler implements CommandHandler {

    private Transmitter transmitter;

    public AckHandler(Transmitter transmitter) {
        this.transmitter = transmitter;
    }

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
