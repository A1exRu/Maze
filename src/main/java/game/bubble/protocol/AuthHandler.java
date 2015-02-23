package game.bubble.protocol;

import game.server.protocol.CommandHandler;
import game.server.udp.Protocol;
import game.server.udp.SessionsHolder;
import game.server.udp.Transmitter;
import game.server.udp.UdpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.UUID;

public class AuthHandler implements CommandHandler {

    private final static Logger LOG = LoggerFactory.getLogger(AuthHandler.class);
    public static final long FAKE_PLAYER_ID = 1L;
    
    private SessionsHolder sessions;
    private Transmitter transmitter;

    public AuthHandler(SessionsHolder sessions, Transmitter transmitter) {
        this.sessions = sessions;
        this.transmitter = transmitter;
    }

    @Override
    public void handle(SocketAddress address, ByteBuffer buff, UUID sessionId) {
        if (sessionId != null) {
            LOG.warn("User already authorized {}", sessionId);
            return;
        }

        UUID token = Protocol.getToken(buff);
        boolean authorized = sessions.authorize(address, token);
        if (authorized) {
            UdpSession session = sessions.get(token);
            ByteBuffer resp = ByteBuffer.allocate(8);
            resp.putInt(1);
            resp.putInt(1);
            transmitter.add(session, resp.array());
            transmitter.add(session, getSingleBubbleInit());
        }
    }

    private byte[] getSingleBubbleInit() {
        ByteBuffer buffer = ByteBuffer.allocate(40);
        buffer.putInt(2); //message code
        buffer.putLong(FAKE_PLAYER_ID); //playerId
        buffer.putInt(200); //x
        buffer.putInt(200); //y
        buffer.putInt(255); //red
        buffer.putInt(0); //green
        buffer.putInt(80); //blue
        buffer.putDouble(0.7); //opacity
        return buffer.array();
    }
}
