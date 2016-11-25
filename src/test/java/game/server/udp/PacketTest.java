package game.server.udp;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PacketTest {

    @Mock
    private UdpConfig config;

    @Before
    public void setup() {
        when(config.getPacketMaxSize()).thenReturn(1000);
        when(config.getPacketAckAwait()).thenReturn(500);
    }
    
    @Test
    public void delta() {
        Packet packet = new Packet(1, new UdpSession(null, null), new byte[2048], config);
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
        Packet packet = new Packet(1, new UdpSession(null, null), new byte[2048], config);
        assertEquals(3, packet.getCapacity());
    }
    
    @Test
    public void countParts() {
        Packet packet = new Packet(1, new UdpSession(null, null), new byte[2048], config);
        assertEquals(1000, packet.countParts(1000, 1));
        assertEquals(1, packet.countParts(1, 1000));
        assertEquals(0, packet.countParts(-1, 1000));
    }
    
    @Test
    public void ack() {
        Packet packet = new Packet(1, new UdpSession(null, null), new byte[2048], config);
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