package game.server.udp;

import game.server.Game;
import game.server.ServerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UdpServer {

    private static final Logger logger = LoggerFactory.getLogger(UdpServer.class);

    private final ServerContext context = new ServerContext();

    private int port;

    private Receiver receiver;
    private Transmitter transmitter;

    private DatagramChannel channel;
    private DatagramSocket socket;

    public UdpServer(int port) throws IOException {
        this.port = port;
        channel = DatagramChannel.open();
        socket = channel.socket();
        SocketAddress address = new InetSocketAddress(port);
        socket.bind(address);

        receiver = new Receiver(channel);
        transmitter = new Transmitter(channel);
    }

    public void start() {
        new Thread(receiver).start();
        new Thread(transmitter).start();
    }

    public void auth() {

    }

    public void send() {

    }

    public void receive(ByteBuffer buff) throws IOException {

    }

    public void stop() throws IOException {
        receiver.stop();
        transmitter.stop();
        socket.close();
        channel.close();
        logger.info("Server stopped");
    }

}