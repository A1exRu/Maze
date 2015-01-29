package game.server.udp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UdpSession {
    
    private static final Logger logger = LoggerFactory.getLogger(UdpSession.class);
    public static long SESSION_TIMEOUT = 300000; //5 min
    
    private final String token;
    private final SocketAddress address;
//    private final long gameId;
//    private final long heroId;
    
    private Map<Long, Packet> packets = new ConcurrentHashMap<>();
    
    private long timeout;

    public UdpSession(String token, SocketAddress address) {
        this.token = token;
        this.address = address;
        prolong();
    }
    
    public boolean isAlive() {
        return timeout > System.currentTimeMillis();
    }
    
    public final long prolong() {
        timeout = System.currentTimeMillis() + SESSION_TIMEOUT;
        return timeout;
    }
    
    public void send(Packet packet) {
        packets.put(packet.id, packet);
    }
    
    public void ack(long packetId, int ackNum) {
        Packet packet = packets.get(packetId);
        if (packet != null) {
            boolean transmit = packet.ack(ackNum);
            if (transmit) {
                packets.remove(packetId);
                logger.debug("Packet {} confirmed", packet.id);
            }
        }
    }
    
    public void submit(ByteBuffer buff, DatagramChannel channel) throws IOException {
        for (Packet packet : packets.values()) {
            if (packet.isTimeout()) {
                byte[][] datagrams = packet.toParts();
                for (byte i = 0; i < datagrams.length; i++) {
                    byte[] datagram = datagrams[i];
                    byte cmd = (i == datagrams.length - 1) ? Protocol.FINAL_PACKAGE : Protocol.PACKAGE; 
                    Protocol.write(buff, packet.id, cmd, i, datagram);
                    channel.send(buff, address);
                }
                
                packet.submit();
            }
        }
    }

    public String getToken() {
        return token;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public long getTimeout() {
        return timeout;
    }
    
}
