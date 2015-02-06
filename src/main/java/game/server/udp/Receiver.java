package game.server.udp;

import game.server.ServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.*;

public class Receiver extends ServerHandler {

    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);
    public static final long FAKE_PLAYER_ID = 1L;

    private final ByteBuffer buff = ByteBuffer.allocate(4096);
    public static final Map<SocketAddress, UdpSession> sessions = new HashMap<>();
    
    private long sessionThreshold = Long.MAX_VALUE;
    
    private Selector selector;
    private DatagramChannel channel;
    
    public Receiver(DatagramChannel channel) {
        this.channel = channel;
    }

    @Override
    public void onStart() {
        try {
            selector = Selector.open();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            logger.error("Initialization error", e);
        }
    }

    @Override
    public void handle() throws IOException {
        invalidate();
        receive();
    }
    
    public void receive() throws IOException {
        selector.select();
        Set<SelectionKey> keys = selector.keys();
        Iterator<SelectionKey> it = keys.iterator();
        while (it.hasNext()) {
            SelectionKey key = it.next();
            if (key.isReadable()) {
                DatagramChannel ch = (DatagramChannel)key.channel();
                buff.clear();
                SocketAddress address = ch.receive(buff);
                work(address);
            }
        }
    }

    public void invalidate() {
        long now = System.currentTimeMillis();
        if (sessionThreshold < now) {
            Set<Map.Entry<SocketAddress, UdpSession>> entries = sessions.entrySet();
            Iterator<Map.Entry<SocketAddress, UdpSession>> it = entries.iterator();
            while (it.hasNext()) {
                Map.Entry<SocketAddress, UdpSession> entry = it.next();
                UdpSession session = entry.getValue();
                if (!session.isAlive()) {
                    it.remove();
                    logger.debug("Session invalidated {}", session.getToken());
                }
            }
        }
    }

    public void work(SocketAddress address) throws IOException{
        buff.flip();

        byte cmd = buff.get();
//        int version = buff.getInt(); //read in protocol method

        switch (cmd) {
            case Protocol.AUTH: {
                UUID token = Protocol.getToken(buff);
                onAuth(address, token);
                break;
            }
            case Protocol.ACK: {
                int version = buff.getInt();
                onAck(address, buff);
                break;
            }
            case Protocol.PING: {
                int version = buff.getInt(); //read in protocol method
                long ping = buff.getLong();
                onPing(address, ping);
                break;
            }
            default: {
                int version = buff.getInt();
//                byte[] datagram = buff.remaining() == 0 ? new byte[0] : Protocol.toDatagram(buff);
                onMessage(address);
            }
        }
    }
    
    public void onAuth(SocketAddress address, UUID token) {
        if (true && !sessions.containsKey(token)) { //validate token
            UdpSession session = new UdpSession(token, address);
            sessions.put(address, session);
            ByteBuffer resp = ByteBuffer.allocate(8);
            resp.putInt(1);
            resp.putInt(1);
            session.send(new Packet(address, resp.array()));
            session.send(new Packet(address, getSingleBubbleInit()));
            sessionThreshold = session.getTimeout();
        }
        
    }
    
    public void onMessage(SocketAddress address) {
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
    
    public void onPing(SocketAddress address, long time) throws IOException {
        Protocol.pong(buff, time, System.currentTimeMillis());
        channel.send(buff, address);
    }
    
    //TEST METHOD
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

    @Override
    public void stop() {
        super.stop();
        selector.wakeup();
    }
}
