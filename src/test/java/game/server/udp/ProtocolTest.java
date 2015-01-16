package game.server.udp;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

public class ProtocolTest {
    
    @Test
    public void auth() {
        final String MESSAGE = "abcdefg";
        
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        Protocol.AUTHENTICATION.write(buffer, MESSAGE.getBytes());

        Protocol protocol = Protocol.values()[buffer.getInt()];
        byte[] result = protocol.toDatagram(buffer);
        assertEquals(MESSAGE, new String(result));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void invalidVersion() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.putInt(Protocol.AUTHENTICATION.ordinal());
        buffer.putInt(-1);
        buffer.flip();
        
        Protocol protocol = Protocol.values()[buffer.getInt()];
        protocol.toDatagram(buffer);
    }
    
}