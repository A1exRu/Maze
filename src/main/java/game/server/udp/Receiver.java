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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Receiver extends ServerHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);
    
    private final ByteBuffer buff = ByteBuffer.allocate(4096);
    public static final Map<SocketAddress, UdpSession> sessions = new ConcurrentHashMap<>();
    
    private long sessionThreshold = Long.MAX_VALUE;
    
    private Selector selector;
    private DatagramChannel channel;
    
    private long temp;
    
    private boolean tempFlag;
    
    public Receiver(DatagramChannel channel) {
        this.channel = channel;
    }

    @Override
    public void onStart() {
        try {
            selector = Selector.open();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        } catch (IOException e) {
            logger.error("Initialization error", e);
        }
    }

    @Override
    public void handle() throws IOException {
        invalidate();
        receive();
        transmit();
        if (!tempFlag && sessions.size() == 1) {
            for (Map.Entry<SocketAddress, UdpSession> entry : sessions.entrySet()) {
                String message = "";
                for (int i = 0; i < 200; i++) {
                    message += "Long long message " + i;
                    
                }
                entry.getValue().send(new Packet(entry.getKey(), message.getBytes()));
                tempFlag = true;
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
    
    public void receive() throws IOException {
        selector.select();
        Set<SelectionKey> keys = selector.keys();
        Iterator<SelectionKey> it = keys.iterator();
        while (it.hasNext()) {
            SelectionKey key = it.next();
            if (key.isReadable()) {
                DatagramChannel ch = (DatagramChannel)key.channel();
                SocketAddress address = ch.receive(buff);
                work(address);
            }
        }
    }
    
    public void work(SocketAddress address) throws IOException{
        buff.flip();

        byte cmd = buff.get();
        byte[] datagram = buff.remaining() == 0 ? new byte[0] : Protocol.toDatagram(buff);

        switch (cmd) {
            case Protocol.AUTH: {
                onAuth(address, new String(datagram));
                break;
            }
            case Protocol.PING: {
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {

                }
                onPing(address);
                break;
            }
            default: {
                onMessage(address, datagram);
            }
        }
    }
    
    public void onAuth(SocketAddress address, String token) {
        if (true) { //validate token
            UdpSession session = new UdpSession(token, address);
            sessions.put(address, session);
            sessionThreshold = session.getTimeout();
        }
        
    }
    
    public void onMessage(SocketAddress address, byte[] datagram) {
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

        //TODO: complete implementation
        System.out.println("User " + session.getToken() + " said: '" + new String(datagram) + "'");
    }
    
    public void onPing(SocketAddress address) throws IOException {
        buff.clear();
        buff.put(Protocol.PING);
        buff.flip();
        channel.send(buff, address);
    }
    
    public void transmit() throws IOException {
        long now = System.currentTimeMillis();
        if (temp > now) {
            return;
        }

        buff.clear();
        buff.put(Protocol.FINAL_PACKAGE);
        buff.put(".".getBytes());
        for (SocketAddress address : sessions.keySet()) {
            buff.flip();
            channel.send(buff, address);
        }

        buff.clear();
        temp = now + 3000;
    }

    @Override
    public void stop() {
        super.stop();
        selector.wakeup();
    }
}
