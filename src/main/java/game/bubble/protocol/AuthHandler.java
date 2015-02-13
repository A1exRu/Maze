package game.bubble.protocol;

import game.server.protocol.CommandHandler;
import game.server.udp.SessionsHolder;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.UUID;

public class AuthHandler implements CommandHandler {

    public static final long FAKE_PLAYER_ID = 1L;
    private SessionsHolder sessions;
    
    public AuthHandler(SessionsHolder sessions) {
        this.sessions = sessions;
    }

    @Override
    public void handle(SocketAddress address, ByteBuffer buff, UUID token) {
        boolean authorized = sessions.authorize(address, token);
        if (authorized) {
            ByteBuffer resp = ByteBuffer.allocate(8);
            resp.putInt(1);
            resp.putInt(1);
            sessions.tell(token, resp.array());
            sessions.tell(token, getSingleBubbleInit());
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
