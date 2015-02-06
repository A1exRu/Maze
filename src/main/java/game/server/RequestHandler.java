package game.server;

import game.core.FMarshaller;
import game.core.Converters;
import game.server.protocol.CommandProcessor;
import game.server.udp.Packet;
import game.server.udp.Protocol;
import game.server.udp.UdpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private final CommandProcessor processor;
    private Converters converters;
    
    public static final Map<UUID, UdpSession> sessions = new HashMap<>();

    public RequestHandler() {
        processor = new CommandProcessor(this::onMessage, true);
        processor.add(Protocol.AUTH, this::onAuth, false);
        processor.add(Protocol.ACK, this::onAck, false);
        processor.add(Protocol.PING, this::onPing, false);
    }

    public void handle(SocketAddress address, ByteBuffer buff)  {
       processor.process(address, buff);
    }

    public void onAuth(SocketAddress address, ByteBuffer buff, UUID sessionUuid) {
        sessionUuid = Protocol.getToken(buff);
        if (!sessions.containsKey(sessionUuid)) { //validate token
            UdpSession session = new UdpSession(sessionUuid, address);
            sessions.put(sessionUuid, session);

            //TODO: remove bubble logic from common class
//            ByteBuffer resp = ByteBuffer.allocate(8);
//            resp.putInt(1);
//            resp.putInt(1);
//            session.send(new Packet(address, resp.array()));
//            session.send(new Packet(address, getSingleBubbleInit()));
        }

    }

    public void onMessage(SocketAddress address, ByteBuffer buff, UUID sessionUuid) {
        UdpSession session = sessions.get(address);
        int type = buff.getInt();

        //TODO: get converter here
        FMarshaller converter = converters.get(type);

        converter.apply(buff);

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

    public void onAck(SocketAddress address, ByteBuffer buff, UUID uuid) {
        UdpSession session = sessions.get(address);
        if (session != null) {
            long packetId = buff.getLong();
            int num = buff.getInt();
            session.ack(packetId, num);
        }
    }

    public void onPing(SocketAddress address, ByteBuffer buff, UUID uuid)  {
        long pingTime = buff.getLong();
        Protocol.pong(buff, pingTime, System.currentTimeMillis());
//        channel.send(buff, address);
    }
    
    
}
