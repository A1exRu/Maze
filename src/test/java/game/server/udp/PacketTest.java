package game.server.udp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PacketTest {
    
    @Test
    public void delta() {
        Packet packet = new Packet(1, new UdpSession(null, null), new byte[2048], true);
        byte[] delta0 = packet.delta(0);
        assertEquals(1000, delta0.length);

        byte[] delta1 = packet.delta(1);
        assertEquals(1000, delta1.length);

        byte[] delta2 = packet.delta(2);
        assertEquals(48, delta2.length);

        byte[] delta3 = packet.delta(3);
        assertEquals(0, delta3.length);

        byte[] deltaInfinity = packet.delta(Integer.MAX_VALUE);
        assertEquals(0, deltaInfinity.length);
        
        byte[] negative = packet.delta(-100);
        assertEquals(0, negative.length);
    }
    
    @Test
    public void capacity() {
        Packet packet = new Packet(1, new UdpSession(null, null), new byte[2048], true);
        assertEquals(3, packet.getCapacity());
    }
    
    @Test
    public void countParts() {
        Packet packet = new Packet(1, new UdpSession(null, null), new byte[2048], true);
        assertEquals(1000, packet.countParts(1000, 1));
        assertEquals(1, packet.countParts(1, 1000));
        assertEquals(0, packet.countParts(-1, 1000));
    }
    
    @Test
    public void ack() {
        Packet packet = new Packet(1, new UdpSession(null, null), new byte[2048], true);
        assertEquals(3, packet.getLeft());
        packet.toParts();

        packet.ack(0);
        assertEquals(2, packet.getLeft());
        
        packet.ack(0);
        packet.ack(10);
        packet.ack(-1);
        assertEquals(2, packet.getLeft());
        
        packet.ack(1);
        assertEquals(1, packet.getLeft());

        packet.ack(2);
        assertEquals(0, packet.getLeft());
    }

}