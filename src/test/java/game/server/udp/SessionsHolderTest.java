package game.server.udp;

import game.server.ServerTime;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.SocketAddress;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class SessionsHolderTest {
    
    private SessionsHolder holder = new SessionsHolder();
    
    @After
    public void after() {
        ServerTime.toDefault();
    }
    
    @Test
    public void testInvalidate() throws Exception {
        ServerTime.toFixed();
        UUID session1 = UUID.randomUUID();
        SocketAddress address1 = Mockito.mock(SocketAddress.class);
        holder.authorize(address1, session1);
        ServerTime.addMills(UdpSession.SESSION_TIMEOUT - 1);
        holder.invalidate();
        assertTrue(holder.hasSession(session1));
        
        UUID session2 = UUID.randomUUID();
        SocketAddress address2 = Mockito.mock(SocketAddress.class);
        holder.authorize(address2, session2);
        
        ServerTime.addMills(2);
        holder.invalidate();

        assertFalse(holder.hasSession(session1));
        assertTrue(holder.hasSession(session2));
    }

    @Test
    public void testCheckSession() throws Exception {
        ServerTime.toFixed();
        UUID sessionId = UUID.randomUUID();
        SocketAddress address = Mockito.mock(SocketAddress.class);
        boolean authorize = holder.authorize(address, sessionId);
        assertTrue(authorize);

        UdpSession session = holder.get(sessionId);
        assertEquals(UdpSession.SESSION_TIMEOUT, session.getTimeout());
        
        ServerTime.addMills(1000);
        assertTrue(holder.checkSession(address, sessionId));
        session = holder.get(sessionId);
        assertEquals(UdpSession.SESSION_TIMEOUT + 1000, session.getTimeout());
        
        assertFalse(holder.checkSession(address, UUID.randomUUID()));

        SocketAddress invalidAddress = Mockito.mock(SocketAddress.class);
        assertFalse(holder.checkSession(invalidAddress, sessionId));
        
        ServerTime.addMills(1000000);
        assertFalse(holder.checkSession(address, sessionId));
    }

    @Test
    public void testAuthorize() throws Exception {
        UUID sessionId = UUID.randomUUID();
        SocketAddress address = Mockito.mock(SocketAddress.class);
        boolean authorize = holder.authorize(address, sessionId);
        assertTrue(authorize);
        assertTrue(holder.hasSession(sessionId));
        assertFalse(holder.hasSession(UUID.randomUUID()));
        
        UdpSession session = holder.get(sessionId);
        assertEquals(session.getTimeout(), holder.getThreshold());
    }

    @Test
    public void testGetWhenNotFound() throws Exception {
        UdpSession session = holder.get(UUID.randomUUID());
        assertNull(session);
    }
}