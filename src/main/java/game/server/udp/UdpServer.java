package game.server.udp;

import game.server.ServerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UdpServer {

    private static final Logger logger = LoggerFactory.getLogger(UdpServer.class);
    
    private final ServerContext context = new ServerContext();
    
    private int port;
    
    private Receiver receiver;
    private DatagramChannel channel;
    
    public UdpServer(int port) throws IOException {
        this.port = port;
        channel = DatagramChannel.open();
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
        channel.close();
        logger.info("Server stopped");
    }
    
}
