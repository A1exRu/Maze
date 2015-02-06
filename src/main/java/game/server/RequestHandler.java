package game.server;

import game.server.protocol.CommandProcessor;
import game.server.udp.Packet;
import game.server.udp.Protocol;
import game.server.udp.UdpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static game.server.udp.Protocol.*;

public class RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
//    private final CommandProcessor processor = new CommandProcessor();
    
    public static final Map<String, UdpSession> sessions = new HashMap<>();
    private long sessionThreshold = Long.MAX_VALUE;
    
    public void handle(SocketAddress address, ByteBuffer buff)  {
        byte command = getCommand(buff);
        
    }

    public void onAuth(SocketAddress address, ByteBuffer buff) {
//        if (true && !sessions.containsKey(token)) { //validate token
//            UdpSession session = new UdpSession(token, address);
//            sessions.put(address, session);
//            ByteBuffer resp = ByteBuffer.allocate(8);
//            resp.putInt(1);
//            resp.putInt(1);
//            session.send(new Packet(address, resp.array()));
//            session.send(new Packet(address, getSingleBubbleInit()));
//            sessionThreshold = session.getTimeout();
//        }

    }

    public void onMessage(SocketAddress address, ByteBuffer buff) {
        UdpSession session = sessions.get(address);
        if (session == null) {
            logger.error("Session not found by address {}", address);
            return;
        }

        if (!session.isAlive()) {
            logger.debug("Session expired");
            return;
        }

        long timeout = session.prolong();
        sessionThreshold = Math.min(sessionThreshold, timeout);
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

            for (UdpSession udpSession : sessions.values()) {
                udpSession.send(new Packet(session.getAddress(), resp.array()));
            }
        } else {
            logger.warn("Invalid command type {}", type);
        }
    }

    public void onAck(SocketAddress address, ByteBuffer buff) {
        UdpSession session = sessions.get(address);
        if (session != null) {
            long packetId = buff.getLong();
            int num = buff.getInt();
            session.ack(packetId, num);
        }
    }

    public void onPing(SocketAddress address, ByteBuffer buff, long time) throws IOException {
        Protocol.pong(buff, time, System.currentTimeMillis());
//        channel.send(buff, address);
    }
    
    
}
