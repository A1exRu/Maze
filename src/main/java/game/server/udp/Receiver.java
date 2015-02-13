package game.server.udp;

import game.server.ServerHandler;
import game.server.protocol.CommandProcessor;
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

    private static final Logger LOG = LoggerFactory.getLogger(Receiver.class);
    public static final long FAKE_PLAYER_ID = 1L;

    private final ByteBuffer buff = ByteBuffer.allocate(4096);
    private final DatagramChannel channel;
    private Selector selector;
    
    private CommandProcessor processor;
    private SessionsHolder sessions = new SessionsHolder();
    
    public Receiver(DatagramChannel channel) {
        this.channel = channel;
        processor = new CommandProcessor(sessions, this::onMessage, true);
        processor.add(Protocol.AUTH, this::onAuth, false);
        processor.add(Protocol.ACK, this::onAck, true);
        processor.add(Protocol.PING, this::onPing, false);
    }

    @Override
    public void onStart() {
        try {
            selector = Selector.open();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            LOG.error("Initialization error", e);
        }
    }

    @Override
    public void handle() throws IOException {
        sessions.invalidate();
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

    public void work(SocketAddress address) throws IOException{
        buff.flip();
        processor.process(address, buff);
        buff.clear();
    }
    
    public void onAuth(SocketAddress address, ByteBuffer buff, UUID token) {
        boolean authorized = sessions.authorize(address, token);
        if (authorized) {
            ByteBuffer resp = ByteBuffer.allocate(8);
            resp.putInt(1);
            resp.putInt(1);
            sessions.tell(token, resp.array());
            sessions.tell(token, getSingleBubbleInit());
        }        
    }
    
    public void onMessage(SocketAddress address, ByteBuffer buff, UUID token) {
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
    
    public void onAck(SocketAddress address, ByteBuffer buff, UUID token) {
        long packetId = buff.getLong();
        int num = buff.getInt();
        sessions.ack(token, packetId, num);
    }
    
    public void onPing(SocketAddress address, ByteBuffer buff, UUID token) {
        long time = buff.getLong();
        Protocol.pong(buff, time, System.currentTimeMillis());
        try {
            channel.send(buff, address);
        } catch (IOException e) {
            LOG.error("Send ping error", e);
        }
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
