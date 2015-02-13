package game.server.udp;

import game.server.ServerHandler;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class Transmitter extends ServerHandler {

    private final ByteBuffer buff = ByteBuffer.allocate(1024);
    public static final AtomicLong packetSequence = new AtomicLong();

    private DatagramChannel channel;

    public Transmitter(DatagramChannel channel) {
        this.channel = channel;
    }

    @Override
    public void handle() throws IOException {
//        Map<SocketAddress, UdpSession> sessions = Receiver.sessions;
//        for (UdpSession session : sessions.values()) {
//            session.submit(buff, channel);
//        }
        
    }
}