package game.server.udp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UdpServer {

    private static final Logger logger = LoggerFactory.getLogger(UdpServer.class);
    
    private int port;
    
    private Receiver receiver;
    
    private DatagramChannel channel;
    private DatagramSocket socket;
    
    private final Map<String, UdpSession> sessions = new ConcurrentHashMap<>();
    
    public UdpServer(int port) throws IOException {
        this.port = port;
        channel = DatagramChannel.open();
        socket = channel.socket();
        SocketAddress address = new InetSocketAddress(port);
        socket.bind(address);
        
        receiver = new Receiver(channel);
    }
    
    public void start() {
        new Thread(receiver).start();
    }
    
    public void auth() {

    }
    
    public void send() {
        
    }
    
    public void receive(ByteBuffer buff) throws IOException {

    }
    
    public void stop() throws IOException {
        receiver.stop();
        socket.close();
        channel.close();
        sessions.clear();
        logger.info("Server stopped");
    }
    
}
