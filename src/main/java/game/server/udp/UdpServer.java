package game.server.udp;

import game.server.ServerTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;

public class UdpServer {

    private static final Logger LOG = LoggerFactory.getLogger(UdpServer.class);

    private final ThreadGroup group = new ThreadGroup("UdpServer");

    @Autowired
    private Receiver receiver;

    @Autowired
    private Transmitter transmitter;

    @Autowired
    private DatagramChannel channel;
    private SocketAddress address;
    private DatagramSocket socket;

    public UdpServer(int port) {
        ServerTime.lockAsProduction();
        address = new InetSocketAddress(port);
    }

    public void start() throws SocketException {
        socket = channel.socket();
        socket.bind(address);

        new Thread(group, receiver).start();
        new Thread(group, transmitter).start();
        LOG.info("Server started on port {}", socket.getLocalPort());
    }

    public void stop() throws IOException {
        channel.close();
        receiver.stop();
        transmitter.stop();
        socket.close();
        LOG.info("Server stopped");
    }

}