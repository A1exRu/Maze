package game.server.udp;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import game.server.ServerHandler;
import game.server.ServerTime;

public class Transmitter extends ServerHandler {
    
    private static final Logger LOG = LoggerFactory.getLogger(Transmitter.class);

    private ByteBuffer buff;
    private final AtomicLong packetSequence = new AtomicLong();

    private Map<Long, Packet> index = new HashMap<>();
    private Queue<Packet> queue;
    private Packet[] packets;
    private int lastIndex = -1;
    private int success;

    @Autowired
    private DatagramChannel channel;
    
    @Autowired
    private UdpConfig config;

    private long threshold;
    
    @PostConstruct
    @Override
    public void onStart() {
        threshold = ServerTime.mills() + config.getGc();
        buff =  ByteBuffer.allocate(config.getOutBufferSize());
        queue = new ArrayBlockingQueue<>(config.getQueueSize());
        packets = new Packet[config.getPacketsSize()];
    }
    
    @Override
    public void handle() throws IOException {
        for (int i = 0; i < packets.length; i++) {
            Packet packet = packets[i];
            if (packet == null) {
                continue;
            }

            if (packet.hasTransmitted()) {
                remove(i, packet);
            } else if (packet.isReady()) {
                transmit(packet);
                if (packet.isNotAckRequired() || !packet.hasAttempts()) {
                    remove(i, packet);
                }
            }
        }
        
        clean();
        merge();
    }

    private void remove(int packIndex, Packet packet) {
        index.remove(packet.id);
        packets[packIndex] = null; //drop from array for gc
        success++;
    }

    public void add(UdpSession session, byte[] datagram) {
        add(session, datagram, config.isAckRequirements());
    }
    
    public void add(UdpSession session, byte[] datagram, boolean ackRequirements) {
        long packetId = packetSequence.incrementAndGet();
        Packet packet = new Packet(packetId, session, datagram, config);
        packet.setAttempts(config.getAttempts());
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
                LOG.error("[ERR-1005]: Transmission failed", e);
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
                LOG.error("[ERR-1004]: Not enough space in packages queue");
                break;
            } else {
                lastIndex++;
                packets[lastIndex] = packet;
                index.put(packet.id, packet);
            }
        }
    }
    
    private void clean() {
        if (success > config.getGc() || threshold > ServerTime.mills()) {
            int k = 0;
            for (int i = lastIndex; i >= k; i--) {
                if (packets[i] != null) {
                    for (int j = k; j < i; j++) {
                        if (packets[j] == null) {
                            packets[j] = packets[i];
                            packets[i] = null;
                            lastIndex--;
                            break;
                        }
                        k++;
                    }
                } else {
                    lastIndex--;
                }
            }
            
            success = 0;
            threshold = ServerTime.mills() + config.getThreshold();
        }
    }

    public int getAttempts() {
        return config.getAttempts();
    }
}