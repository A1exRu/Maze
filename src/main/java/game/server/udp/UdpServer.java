package game.server.udp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UdpServer {
    
    private int port;
    
    private DatagramChannel channel;
    private DatagramSocket socket;
    private ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
    
    private Map<String, UdpSession> sessions = new ConcurrentHashMap<>();
    
    public UdpServer(int port) throws IOException {
        this.port = port;
        channel = DatagramChannel.open();
        socket = channel.socket();
        SocketAddress address = new InetSocketAddress(port);
        socket.bind(address);
    }
    
    public void auth() {

    }
    
    public void send() {
        
    }
    
    public void receive() throws IOException {

    }
    
    public void stop() throws IOException {
        socket.close();
        channel.close();
        sessions.clear();
    }
    
}
