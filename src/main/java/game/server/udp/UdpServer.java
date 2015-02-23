package game.server.udp;

import game.server.ServerContext;
import game.server.ServerTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

public class UdpServer {

    private static final Logger LOG = LoggerFactory.getLogger(UdpServer.class);

    private final ThreadGroup group = new ThreadGroup("UdpServer");
    private final ServerContext context = new ServerContext();
    private int port;

    private Receiver receiver;
    private Transmitter transmitter;

    private DatagramChannel channel;
    private DatagramSocket socket;

    public UdpServer(int port) throws IOException {
        ServerTime.lockAsProduction();
        this.port = port;
        channel = DatagramChannel.open();
        socket = channel.socket();
        SocketAddress address = new InetSocketAddress(port);
        socket.bind(address);
        

        transmitter = new Transmitter(channel);
        receiver = new Receiver(channel, transmitter);
    }

    public void start() {
        new Thread(group, receiver).start();
        new Thread(group, transmitter).start();
    }

    public void stop() throws IOException {
        receiver.stop();
        transmitter.stop();
        socket.close();
        channel.close();
        LOG.info("Server stopped");
    }

}