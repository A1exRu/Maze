package game.server.udp;

import game.server.ServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class Transmitter extends ServerHandler {
    
    private static final Logger LOG = LoggerFactory.getLogger(Transmitter.class);

    private final ByteBuffer buff = ByteBuffer.allocate(1024);
    public static final AtomicLong packetSequence = new AtomicLong();

    private DatagramChannel channel;
    
    private Map<Long, Packet> packets = new HashMap<>();
    private Queue<Packet> queue = new ArrayBlockingQueue<>(1024);
    
    public Transmitter(DatagramChannel channel) {
        this.channel = channel;
    }

    @Override
    public void handle() throws IOException {
        Collection<Packet> outList = packets.values();
        outList.removeIf(Packet::hasTransmitted);
        outList.stream()
                .filter(Packet::isReady)
                .forEach(this::transmit);
        merge();
    }

    public void add(UdpSession session, byte[] datagram) {
        long packetId = packetSequence.incrementAndGet();
        Packet packet = new Packet(packetId, session, datagram);
        queue.add(packet);
    }
    
    public void ack(UUID sessionId, long packetId, int part) {
        Packet packet = packets.get(packetId);
        if (packet != null && sessionId.equals(packet.getSessionId())) {
            if (sessionId.equals(packet.getSessionId())) {
                packet.ack(part);
            } else {
                LOG.error("[ERR-403]: Received ack from not authorized client with sessionId={}", sessionId);
            }
        } 
    }    
    
    private void transmit(Packet packet) {
        SocketAddress address = packet.getAddress();
        byte[][] datagrams = packet.toParts();
        for (byte i = 0; i < datagrams.length; i++) {
            byte[] datagram = datagrams[i];
            byte cmd = (i == datagrams.length - 1) ? Protocol.FINAL_PACKAGE : Protocol.PACKAGE;
            Protocol.write(buff, packet.id, cmd, i, datagram);
            try {
                channel.send(buff, address);
            } catch (IOException e) {
                LOG.error("Transmission failed", e);
            }
        }

        packet.submit();    
    }

    private void merge() {
        Packet packet;
        while ((packet = queue.poll()) != null) {
            packets.put(packet.id, packet);
        }
    }
}