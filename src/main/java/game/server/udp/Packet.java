package game.server.udp;

import java.net.SocketAddress;
import java.util.BitSet;

public class Packet {
    
    private static final int DELTA = 10;
    private static final int AWAIT = 5000;
    public static final byte[] EMPTY = new byte[0];

    public final long id;
    public final SocketAddress address;
    private final byte[] datagram;
    private final BitSet confirms;
    private final int capacity;
    private int left;
    private long timeout;
    
    public Packet(SocketAddress address, byte[] datagram) {
        this.id = Transmitter.packetSequence.incrementAndGet();
        this.address = address;
        this.datagram = datagram;
        this.capacity = countParts(datagram.length, DELTA);
        confirms = new BitSet();
        confirms.set(0, capacity, true);
        this.left = capacity;
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
        
        int from = DELTA * partNumber;
        if (from >= datagram.length) {
            return EMPTY;
        }

        int to = Math.min(from + DELTA, datagram.length);
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
        timeout = System.currentTimeMillis() + AWAIT;
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
        return timeout < System.currentTimeMillis();
    }

    public int getCapacity() {
        return capacity;
    }

    public int getLeft() {
        return left;
    }
    
}
