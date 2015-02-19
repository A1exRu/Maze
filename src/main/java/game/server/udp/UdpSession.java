package game.server.udp;

import game.server.ServerTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UdpSession {
    
    private static final Logger logger = LoggerFactory.getLogger(UdpSession.class);
    public static long SESSION_TIMEOUT = 300000; //5 min
    
    private final UUID id;
    private final SocketAddress address;
//    private final long gameId;
//    private final long heroId;
    
    private Map<Long, Packet> packets = new HashMap<>();
    
    private long timeout;

    public UdpSession(UUID id, SocketAddress address) {
        this.id = id;
        this.address = address;
        prolong();
    }
    
    public boolean isAlive() {
        return timeout > ServerTime.mills();
    }
    
    public final long prolong() {
        timeout = ServerTime.mills() + SESSION_TIMEOUT;
        return timeout;
    }
    
//    public void send(Packet packet) {
//        packets.put(packet.id, packet);
//    }
//
//    public void ack(long packetId, int ackNum) {
//        Packet packet = packets.get(packetId);
//        if (packet != null) {
//            boolean transmit = packet.ack(ackNum);
//            if (transmit) {
//                packets.remove(packetId);
//                logger.debug("Packet {} confirmed", packet.id);
//            }
//        }
//    }
//
//    public void submit(ByteBuffer buff, DatagramChannel channel) throws IOException {
//        for (Packet packet : packets.values()) {
//            if (packet.isTimeout()) {
//                byte[][] datagrams = packet.toParts();
//                for (byte i = 0; i < datagrams.length; i++) {
//                    byte[] datagram = datagrams[i];
//                    byte cmd = (i == datagrams.length - 1) ? Protocol.FINAL_PACKAGE : Protocol.PACKAGE;
//                    Protocol.write(buff, packet.id, cmd, i, datagram);
//                    channel.send(buff, address);
//                }
//
//                packet.submit();
//            }
//        }
//    }

    public UUID getId() {
        return id;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public long getTimeout() {
        return timeout;
    }
    
}
