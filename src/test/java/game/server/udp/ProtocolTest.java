package game.server.udp;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class ProtocolTest {

    private ByteBuffer buffer = ByteBuffer.allocate(1024);
    
    @Before
    public void setup() {
        buffer.clear();
    }
    
    @Test
    public void auth() {
        UUID id = UUID.randomUUID();
        Protocol.auth(buffer, id);
        
        byte cmd = Protocol.getCommand(buffer);
        assertEquals(Protocol.AUTH, cmd);
        
        UUID token = Protocol.getToken(buffer);
        assertEquals(id, token);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidVersion() {
        buffer.putInt(Protocol.AUTH);
        buffer.putInt(-1);
        buffer.flip();

        Protocol.getCommand(buffer);
    }
    
}