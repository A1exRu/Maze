package game.server.udp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Receiver implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);
    
    private final ByteBuffer buff = ByteBuffer.allocate(1024);
    private final Map<SocketAddress, UdpSession> sessions = new ConcurrentHashMap<>();
    
    private DatagramChannel channel;
    private boolean terminated;
    
    public Receiver(DatagramChannel channel) {
        this.channel = channel;
    }

    @Override
    public void run() {
        while (!terminated) {
            try {
                work();   
            } catch (Exception e) {
                logger.error("Receiver error", e);
            }
        }
    }
    
    public void work() throws IOException{
        SocketAddress address = channel.receive(buff);
        buff.flip();

        int operation = buff.getInt();
        Protocol protocol = Protocol.valueOf(operation);
        byte[] datagram = protocol.toDatagram(buff);

        switch (protocol) {
            case AUTHENTICATION: {
                onAuth(address, new String(datagram));
                break;
            }
            default: {
                onMessage(address, datagram);
            }
        }
    }
    
    public void onAuth(SocketAddress address, String token) {
        if (true) { //validate token
            UdpSession session = new UdpSession(token, channel.socket());
            sessions.put(address, session);
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
        
        session.prolong();
        
        //TODO: complete implementation
        System.out.println("User " + session.getToken() + " said: '" + new String(datagram) + "'");
    }
    
    public void stop() {
        terminated = true;
    }
}
