package game.server.protocol;

import game.server.udp.Protocol;
import game.server.udp.SessionsHolder;
import game.server.udp.UdpSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CommandProcessorTest {

    @Spy
    public SessionsHolder sessionsHolder = new SessionsHolder();
    
    @Mock
    private CommandHandler handler;
    
    @InjectMocks
    private CommandProcessor processor;
    
    @Test
    public void testSupportedCommand() {
        CommandHandler authCmd = Mockito.mock(CommandHandler.class);
        CommandHandler ackCmd = Mockito.mock(CommandHandler.class);
        processor.add(Protocol.AUTH, authCmd);
        processor.add(Protocol.ACK, ackCmd);
        
        SocketAddress address = Mockito.mock(SocketAddress.class);
        ByteBuffer buff = ByteBuffer.allocate(1024);
        Protocol.auth(buff, UUID.randomUUID());
        
        processor.process(address, buff);
        verify(authCmd).handle(address, buff, null);
        verify(handler, never()).handle(address, buff, null);
        verify(ackCmd, never()).handle(address, buff, null);
    }
    
    @Test
    public void testDefaultCommand() {
        SocketAddress address = Mockito.mock(SocketAddress.class);
        ByteBuffer buff = ByteBuffer.allocate(1024);
        Protocol.auth(buff, UUID.randomUUID());
        processor.process(address, buff);
        verify(handler).handle(address, buff, null);
    }

    @Test
    public void testAuthRequiredCommand() {
        CommandHandler authCmd = Mockito.mock(CommandHandler.class);
        CommandHandler ackCmd = Mockito.mock(CommandHandler.class);
        when(ackCmd.isAuthRequired()).thenReturn(true);
        processor.add(Protocol.AUTH, authCmd);
        processor.add(Protocol.ACK, ackCmd);

        SocketAddress address = Mockito.mock(SocketAddress.class);
        UUID token = UUID.randomUUID();
        ByteBuffer buff = getAck(token);
        sessionsHolder.authorize(address, token);

        processor.process(address, buff);
        verify(handler, never()).handle(address, buff, token);
        verify(authCmd, never()).handle(address, buff, token);
        verify(ackCmd, times(1)).handle(address, buff, token);
        
        assertEquals(21, buff.position());
        assertEquals(12, buff.remaining());
    }

    @Test
    public void testAuthRequiredDefaultCommand() {
        SocketAddress address = Mockito.mock(SocketAddress.class);
        ByteBuffer buff = ByteBuffer.allocate(1024);
        UUID token = UUID.randomUUID();
        Protocol.auth(buff, token);
        sessionsHolder.authorize(address, token);
        when(handler.isAuthRequired()).thenReturn(true);

        processor.process(address, buff);
        verify(handler, times(1)).handle(address, buff, token);
    }

    @Test
    public void testAuthFailedCommand() {
        CommandHandler authCmd = Mockito.mock(CommandHandler.class);
        CommandHandler ackCmd = Mockito.mock(CommandHandler.class);
        when(authCmd.isAuthRequired()).thenReturn(true);
        processor.add(Protocol.AUTH, authCmd);
        processor.add(Protocol.ACK, ackCmd);

        SocketAddress address = Mockito.mock(SocketAddress.class);
        ByteBuffer buff = ByteBuffer.allocate(1024);
        UUID uuid = UUID.randomUUID();
        Protocol.auth(buff, uuid);

        processor.process(address, buff);
        verify(handler, never()).handle(address, buff, uuid);
        verify(authCmd, never()).handle(address, buff, uuid);
        verify(ackCmd, never()).handle(address, buff, uuid);
    }

    @Test
    public void testInvalidAddressCommand() {
        CommandHandler authCmd = Mockito.mock(CommandHandler.class);
        CommandHandler ackCmd = Mockito.mock(CommandHandler.class);
        when(ackCmd.isAuthRequired()).thenReturn(true);
        processor.add(Protocol.AUTH, authCmd);
        processor.add(Protocol.ACK, ackCmd);

        SocketAddress address = Mockito.mock(SocketAddress.class);
        UUID token = UUID.randomUUID();
        ByteBuffer buff = getAck(token);
        sessionsHolder.authorize(address, token);

        SocketAddress anotherAddress = Mockito.mock(SocketAddress.class);
        processor.process(anotherAddress, buff);
        verify(handler, never()).handle(address, buff, token);
        verify(authCmd, never()).handle(address, buff, token);
        verify(ackCmd, never()).handle(address, buff, token);

        assertEquals(21, buff.position());
        assertEquals(12, buff.remaining());
    }
    
    @Test
    public void testAuthFailedDefaultCommand() {
        SocketAddress address = Mockito.mock(SocketAddress.class);
        ByteBuffer buff = ByteBuffer.allocate(1024);
        UUID uuid = UUID.randomUUID();
        Protocol.auth(buff, uuid);

        processor.process(address, buff);
        verify(handler, never()).handle(address, buff, uuid);
    }
    
    
    @Test(expected = IllegalStateException.class)
    public void testAddTwice() throws Exception {
        CommandHandler authCmd = Mockito.mock(CommandHandler.class);
        processor.add(Protocol.AUTH, authCmd);
        processor.add(Protocol.ACK, authCmd);
        processor.add(Protocol.AUTH, authCmd);
    }

    @Test
    public void testValidate() throws Exception {
        CommandHandler authCmd = Mockito.mock(CommandHandler.class);
        processor.add(Protocol.AUTH, authCmd);
        processor.add(Protocol.ACK, (a,b,c) -> {});
        processor.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void testValidateFailed() throws Exception {
        CommandHandler authCmd = Mockito.mock(CommandHandler.class);
        processor.add(Protocol.AUTH, authCmd);
        processor.add(Protocol.ACK, authCmd);
        processor.validate();
    }
    
    @Test
    public void testInvalidatedSession() {
        CommandHandler authCmd = Mockito.mock(CommandHandler.class);
        CommandHandler ackCmd = Mockito.mock(CommandHandler.class);
        when(ackCmd.isAuthRequired()).thenReturn(true);
        processor.add(Protocol.AUTH, authCmd);
        processor.add(Protocol.ACK, ackCmd);

        SocketAddress address = Mockito.mock(SocketAddress.class);
        UUID token = UUID.randomUUID();
        ByteBuffer buff = getAck(token);
        
        when(sessionsHolder.checkSession(address, token)).thenReturn(false);
        sessionsHolder.authorize(address, token);

        processor.process(address, buff);
        verify(handler, never()).handle(address, buff, token);
        verify(authCmd, never()).handle(address, buff, token);
        verify(ackCmd, never()).handle(address, buff, token);

        assertEquals(21, buff.position());
        assertEquals(12, buff.remaining());
    }

    private ByteBuffer getAck(UUID token) {
        ByteBuffer buff = ByteBuffer.allocate(1024);
        buff.put(Protocol.ACK);
        buff.putInt(Protocol.VERSION);
        buff.putLong(token.getMostSignificantBits());
        buff.putLong(token.getLeastSignificantBits());
        buff.putLong(1L);
        buff.putInt(2);
        buff.flip();
        return buff;
    }
}