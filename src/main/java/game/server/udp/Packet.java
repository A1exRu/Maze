package game.server.udp;

import java.net.SocketAddress;
import java.util.BitSet;
import java.util.UUID;

import game.server.ServerTime;

public class Packet {

    public static final byte[] EMPTY = new byte[0];

    public final long id;
    public final UUID sessionId;
    public final SocketAddress address;
    private final byte[] datagram;
    private final BitSet confirms;
    private final int capacity;
    private int left;
    private long timeout;
    
    private int await;
    private int maxSize;
    
    private boolean ackRequired;
    private int attempts;
    
    public Packet(long id, UdpSession session, byte[] datagram, UdpConfig config) {
        this.id = id;
        this.sessionId = session.getId();
        this.address = session.getAddress();
        this.datagram = datagram;
        this.await = config.getPacketAckAwait();
        this.maxSize = config.getPacketMaxSize();
        this.capacity = countParts(datagram.length, maxSize);
        confirms = new BitSet();
        confirms.set(0, capacity, true);
        this.left = capacity;
        this.ackRequired = config.isAckRequirements();
    }
    
    byte[][] toParts() {
        byte[][] parts = new byte[capacity][0];
        confirms.stream().forEach(i -> parts[i] = delta(i));
        return parts;
    }

    byte[] delta(int partNumber) {
        if (isOutOfRange(partNumber)) {
            return EMPTY;
        }
        
        int from = maxSize * partNumber;
        if (from >= datagram.length) {
            return EMPTY;
        }

        int to = Math.min(from + maxSize, datagram.length);
        byte[] part = new byte[to - from];
        for (int i = 0; i < part.length; i++) {
            part[i] = datagram[from + i];
        }
        
        return part;
    }

    boolean ack(int partNumber) {
        if (isOutOfRange(partNumber)) {
            return false;
        }
        
        if (confirms.get(partNumber)) {
            confirms.set(partNumber, false);
            left--;
        }
        
        return left == 0;
    }
    
    public void submit() {
        timeout = ServerTime.mills() + await;
        attempts--;
    }
    
    final int countParts(int length, int delta) {
        int result = 0;
        while (length > 0) {
            length -= delta;
            result++;
        }
        
        return result;
    }
    
    private boolean isOutOfRange(int partNumber) {
        return partNumber < 0 || partNumber > capacity;
    }
    
    public boolean isTimeout() {
        return timeout < ServerTime.mills();
    }

    public boolean hasTransmitted() {
        return left == 0;
    }
    
    public boolean isReady() {
        return !hasTransmitted() && isTimeout();
    }

    public boolean isNotAckRequired() {
        return !ackRequired;
    }
    
    public boolean hasAttempts() {
        return attempts > 0;
    }
    
    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getLeft() {
        return left;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public boolean isAckRequired() {
        return ackRequired;
    }

    public int getAwait() {
        return await;
    }

    public void setAwait(int await) {
        this.await = await;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Packet packet = (Packet) o;

        if (id != packet.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
