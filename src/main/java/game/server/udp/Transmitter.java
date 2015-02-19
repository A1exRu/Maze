package game.server.udp;

import game.server.ServerHandler;
import game.server.ServerTime;
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
    private final AtomicLong packetSequence = new AtomicLong();

    private DatagramChannel channel;
    
    private Map<Long, Packet> index = new HashMap<>();
    private Queue<Packet> queue = new ArrayBlockingQueue<>(1024);
    private Packet[] packets = new Packet[2048];
    private int lastIndex = -1;
    private int success;
    
    private int GC_THRESHOLD = 1000;
    private long threshold;
    
    
    public Transmitter(DatagramChannel channel) {
        this.channel = channel;
        threshold = ServerTime.mills() + GC_THRESHOLD;
    }

    @Override
    public void handle() throws IOException {
        for (int i = 0; i < packets.length; i++) {
            Packet packet = packets[i];
            if (packet == null) {
                continue;
            }

            if (packet.hasTransmitted()) {
                index.remove(packet.id);
                packets[i] = null; //drop from array for gc
                success++;
            } else if (packet.isReady()) {
                transmit(packet);
            }
        }
        
        clean();
        merge();
    }

    public void add(UdpSession session, byte[] datagram) {
        long packetId = packetSequence.incrementAndGet();
        Packet packet = new Packet(packetId, session, datagram);
        queue.add(packet);
    }
    
    public void ack(UUID sessionId, long packetId, int part) {
        Packet packet = index.get(packetId);
        if (packet != null) {
            if (sessionId.equals(packet.getSessionId())) {
                packet.ack(part);
            } else {
                LOG.error("[ERR-403]: Received ack from not authorized client with sessionId={}", sessionId);
            }
        } 
    }    
    
    public int queueSize() {
        return queue.size();
    }

    public int packetsSize() {
        return lastIndex + 1 - success;
    }
    
    private void transmit(Packet packet) {
        SocketAddress address = packet.getAddress();
        byte[][] datagrams = packet.toParts();
        for (byte i = 0; i < datagrams.length; i++) {
            byte[] datagram = datagrams[i];
            if (datagram.length == 0) {
                continue;
            }
            
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
        if (queue.isEmpty()) {
            return;
        }

        int size = queue.size();
        if (lastIndex + size >= packets.length) {
            clean();
        }
        
        Packet packet;
        while ((packet = queue.poll()) != null) {
            if (lastIndex == packets.length - 1) {
                //TODO: fix extension
                break;
            } else {
                lastIndex++;
                packets[lastIndex] = packet;
                index.put(packet.id, packet);
            }
        }
    }
    
    private void clean() {
        if (success > GC_THRESHOLD || threshold > ServerTime.mills()) {
            int k = 0;
            for (int i = lastIndex; i >= k; i--) {
                if (packets[i] != null) {
                    for (int j = k; j < i; j++) {
                        if (packets[j] == null) {
                            packets[j] = packets[i];
                            packets[i] = null;
                            k = j;
                            lastIndex--;
                            break;
                        }
                    }
                } else {
                    lastIndex--;
                }
            }
            
            
            threshold = ServerTime.mills() + GC_THRESHOLD;
        }
    }
}