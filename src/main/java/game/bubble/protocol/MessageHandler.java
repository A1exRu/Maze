package game.bubble.protocol;

import game.server.protocol.CommandHandler;
import game.server.udp.SessionsHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.UUID;

public class MessageHandler implements CommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MessageHandler.class);

    private SessionsHolder sessions;

    public MessageHandler(SessionsHolder sessions) {
        this.sessions = sessions;
    }
    
    @Override
    public boolean isAuthRequired() {
        return true;
    }

    @Override
    public void handle(SocketAddress address, ByteBuffer buff, UUID sessionUuid) {
        int type = buff.getInt();
        if (type == 3) {
            ByteBuffer resp = ByteBuffer.allocate(28);
            long playerId = buff.getLong();
            double dx = buff.getDouble();
            double dy = buff.getDouble();
            resp.putInt(3);
            resp.putLong(playerId);
            resp.putDouble(dx);
            resp.putDouble(dy);

//            for (UdpSession udpSession : sessions.values()) {
//                udpSession.send(new Packet(session.getAddress(), resp.array()));
//            }
        } else {
            LOG.warn("Invalid command type {}", type);
        }

    }
}
