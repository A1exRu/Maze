package game.server.udp;

import java.net.SocketAddress;

public class Packet {

    private static final int DELTA = 1000;
    public static final byte[] EMPTY = new byte[0];

    public final long id;
    public final SocketAddress address;
    private final byte[] datagram;
    private final boolean[] confirm;
    private final int capacity;
    private int left;

    
    public Packet(SocketAddress address, byte[] datagram) {
        this.id = Transmitter.packetSequence.incrementAndGet();
        this.address = address;
        this.datagram = datagram;
        this.capacity = countParts(datagram.length, DELTA);
        this.confirm = new boolean[capacity];
        this.left = capacity;
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

    void ack(int partNumber) {
        if (isOutOfRange(partNumber)) {
            return;
        }
        
        if (!confirm[partNumber]) {
            confirm[partNumber] = true;
            left--;
        }
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

    public int getCapacity() {
        return capacity;
    }

    public int getLeft() {
        return left;
    }
}
