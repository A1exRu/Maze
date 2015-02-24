package game.server.udp;

import game.server.ServerTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TransmitterTest {
    
    @InjectMocks
    private Transmitter transmitter;
    
    @Mock
    private UdpSession session;
    
    @Mock
    private DatagramChannel channel;
    
    @Mock
    private UdpConfig config;
    
    @Before
    public void setup() {
        when(config.getOutBufferSize()).thenReturn(1024);
        when(config.getQueueSize()).thenReturn(1024);
        when(config.getPacketsSize()).thenReturn(2048);
        when(config.isAckRequirements()).thenReturn(true);
        when(config.getAttempts()).thenReturn(5);
        when(config.getGc()).thenReturn(100);
        when(config.getThreshold()).thenReturn(1000);
        when(config.getPacketMaxSize()).thenReturn(1000);
        when(config.getPacketAckAwait()).thenReturn(500);
        transmitter.onStart();
        doReturn(UUID.randomUUID()).when(session).getId();
    }
    
    @After
    public void after() {
        ServerTime.toDefault();
    }
    
    @Test
    public void transmit() throws Exception {
        String message = "Hello UDP";
        transmitter.add(session, message.getBytes());
        assertEquals(1, transmitter.queueSize());
        transmitter.handle();
        verify(channel, never()).send(any(), any());
        
        assertEquals(0, transmitter.queueSize());
        int packetsSize = transmitter.packetsSize();
        assertEquals(1, packetsSize);
        transmitter.handle();

        ArgumentCaptor<ByteBuffer> captor = ArgumentCaptor.forClass(ByteBuffer.class);
        verify(channel).send(captor.capture(), any());
        ByteBuffer buffer = captor.getValue();

        byte cmd = buffer.get();
        int version = buffer.getInt();
        long packId = buffer.getLong();
        int packNum = buffer.getInt();
        
        
        assertEquals(Protocol.FINAL_PACKAGE, cmd);
        assertEquals(1, version);
        assertEquals(1L, packId);
        assertEquals(0, packNum);
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        assertEquals(message, new String(data));
    }
    
    @Test
    public void transmitLongMessage() throws IOException {
        String message = getLongMessage();
        transmitter.add(session, message.getBytes());
        transmitter.handle();
        verify(channel, never()).send(any(), any());

        transmitter.handle();
        verify(channel, times(2)).send(any(), any());
    }
    
    @Test
    public void transmitAfterTimeout() throws IOException {
        ServerTime.toFixed();
        String message = "Hello UDP";
        transmitter.add(session, message.getBytes());
        transmitter.handle();
        verify(channel, never()).send(any(), any());

        ServerTime.addMills(1);
        transmitter.handle();
        verify(channel).send(any(), any());

        transmitter.handle();
        verify(channel).send(any(), any());

        ServerTime.addMills(config.getPacketAckAwait() - 2);
        transmitter.handle();
        verify(channel).send(any(), any());

        ServerTime.addMills(3);
        transmitter.handle();
        verify(channel, times(2)).send(any(), any());
    }

    @Test
    public void transmitAttempts() throws IOException {
        ServerTime.toFixed();
        String message = "Hello UDP";
        transmitter.add(session, message.getBytes());
        transmitter.handle();
        verify(channel, never()).send(any(), any());

        ServerTime.addMills(1);
        int attempts = transmitter.getAttempts();
        for (int i = 0; i < attempts; i++){
            assertEquals(1, transmitter.packetsSize());
            transmitter.handle();
            verify(channel, times(i + 1)).send(any(), any());
            ServerTime.addMills(config.getPacketAckAwait() + 1);
        }
        
        transmitter.handle();
        verify(channel, times(attempts)).send(any(), any());
        assertEquals(0, transmitter.queueSize());
        assertEquals(0, transmitter.packetsSize());
    }
    
    @Test
    public void ack() throws IOException {
        ServerTime.toFixed();
        String message = getLongMessage();
        transmitter.add(session, message.getBytes());
        transmitter.handle();
        ServerTime.addMills(1);
        transmitter.handle();
        verify(channel, times(2)).send(any(), any());
        
        transmitter.ack(session.getId(), 1L, 0);
        ServerTime.addMills(config.getPacketAckAwait() + 1);
        transmitter.handle();
        verify(channel, times(3)).send(any(), any());

        transmitter.ack(session.getId(), 1L, 0);
        ServerTime.addMills(config.getPacketAckAwait() + 1);
        transmitter.handle();
        verify(channel, times(4)).send(any(), any());

        transmitter.ack(session.getId(), 1L, 1);
        ServerTime.addMills(config.getPacketAckAwait() + 1);
        transmitter.handle();
        verify(channel, times(4)).send(any(), any());

        ServerTime.addMills(config.getPacketAckAwait() + 1);
        transmitter.handle();
        verify(channel, times(4)).send(any(), any());

        assertEquals(0, transmitter.queueSize());
        assertEquals(0, transmitter.packetsSize());
    }

    @Test
    public void ackNotRequired() throws IOException {
        when(config.isAckRequirements()).thenReturn(false);
        ServerTime.toFixed();
        String message = "Hello UDP";
        transmitter.add(session, message.getBytes());
        transmitter.handle();
        ServerTime.addMills(1);
        transmitter.handle();
        verify(channel, times(1)).send(any(), any());

        assertEquals(0, transmitter.queueSize());
        assertEquals(0, transmitter.packetsSize());
    }

    @Test
    public void ackOnSingle() throws IOException {
        ServerTime.toFixed();
        String message = "Hello UDP";
        transmitter.add(session, message.getBytes());
        assertEquals(1, transmitter.queueSize());
        assertEquals(0, transmitter.packetsSize());
        transmitter.handle();
        
        ServerTime.addMills(1);
        transmitter.handle();
        
        transmitter.ack(session.getId(), 1L, 0);
        ServerTime.addMills(config.getPacketAckAwait() + 1);
        
        transmitter.handle();
        verify(channel).send(any(), any());

        assertEquals(0, transmitter.queueSize());
        assertEquals(0, transmitter.packetsSize());

    }

    @Test
    public void ackWithInvalidSession() throws IOException {
        ServerTime.toFixed();
        String message = "Hello UDP";
        transmitter.add(session, message.getBytes());
        assertEquals(1, transmitter.queueSize());
        assertEquals(0, transmitter.packetsSize());
        transmitter.handle();

        ServerTime.addMills(1);
        transmitter.handle();
        verify(channel).send(any(), any());

        transmitter.ack(UUID.randomUUID(), 1L, 0);
        ServerTime.addMills(config.getPacketAckAwait() + 1);
        transmitter.handle();
        verify(channel, times(2)).send(any(), any());
    }
    
    private String getLongMessage() {
        StringBuilder builder = new StringBuilder(2000);
        for (int i = 0; i < builder.capacity(); i++) {
            builder.append("X");
        }
        
        return builder.toString();
    }
    

}