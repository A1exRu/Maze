package game.server.protocol;

import game.server.udp.Protocol;
import game.server.udp.UdpSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CommandProcessorTest {

    @Mock
    private CommandHandler handler;

    @Test
    public void testSupportedCommand() {
        CommandProcessor processor = new CommandProcessor(handler, false);
        
        CommandHandler authCmd = Mockito.mock(CommandHandler.class);
        CommandHandler ackCmd = Mockito.mock(CommandHandler.class);
        processor.add(Protocol.AUTH, authCmd, false);
        processor.add(Protocol.ACK, ackCmd, false);
        
        SocketAddress address = Mockito.mock(SocketAddress.class);
        ByteBuffer buff = ByteBuffer.allocate(1024);
        Protocol.auth(buff, UUID.randomUUID());
        
        processor.process(address, buff);
        verify(authCmd).handle(address, buff);
        verify(handler, never()).handle(address, buff);
        verify(ackCmd, never()).handle(address, buff);
    }
    
    @Test
    public void testDefaultCommand() {
        SocketAddress address = Mockito.mock(SocketAddress.class);
        ByteBuffer buff = ByteBuffer.allocate(1024);
        Protocol.auth(buff, UUID.randomUUID());
        CommandProcessor processor = new CommandProcessor(handler, false);
        processor.process(address, buff);
        verify(handler).handle(address, buff);
    }

    @Test
    public void testAuthRequiredCommand() {
        CommandProcessor processor = new CommandProcessor(handler, true);
        CommandHandler authCmd = Mockito.mock(CommandHandler.class);
        CommandHandler ackCmd = Mockito.mock(CommandHandler.class);
        processor.add(Protocol.AUTH, authCmd, false);
        processor.add(Protocol.ACK, ackCmd, true);

        SocketAddress address = Mockito.mock(SocketAddress.class);
        UUID token = UUID.randomUUID();
        ByteBuffer buff = getAck(token);
        processor.sessions.put(token, new UdpSession(token, address));

        processor.process(address, buff);
        verify(handler, never()).handle(address, buff);
        verify(authCmd, never()).handle(address, buff);
        verify(ackCmd, times(1)).handle(address, buff);
        
        assertEquals(21, buff.position());
        assertEquals(12, buff.remaining());
    }

    @Test
    public void testAuthRequiredDefaultCommand() {
        CommandProcessor processor = new CommandProcessor(handler, true);
        SocketAddress address = Mockito.mock(SocketAddress.class);
        ByteBuffer buff = ByteBuffer.allocate(1024);
        UUID token = UUID.randomUUID();
        Protocol.auth(buff, token);
        processor.sessions.put(token, new UdpSession(token, address));

        processor.process(address, buff);
        verify(handler, times(1)).handle(address, buff);
    }

    @Test
    public void testAuthFailedCommand() {
        CommandProcessor processor = new CommandProcessor(handler, false);
        CommandHandler authCmd = Mockito.mock(CommandHandler.class);
        CommandHandler ackCmd = Mockito.mock(CommandHandler.class);
        processor.add(Protocol.AUTH, authCmd, true);
        processor.add(Protocol.ACK, ackCmd, false);

        SocketAddress address = Mockito.mock(SocketAddress.class);
        ByteBuffer buff = ByteBuffer.allocate(1024);
        UUID uuid = UUID.randomUUID();
        Protocol.auth(buff, uuid);

        processor.process(address, buff);
        verify(handler, never()).handle(address, buff);
        verify(authCmd, never()).handle(address, buff);
        verify(ackCmd, never()).handle(address, buff);
    }

    @Test
    public void testInvalidAddressCommand() {
        CommandProcessor processor = new CommandProcessor(handler, true);
        CommandHandler authCmd = Mockito.mock(CommandHandler.class);
        CommandHandler ackCmd = Mockito.mock(CommandHandler.class);
        processor.add(Protocol.AUTH, authCmd, false);
        processor.add(Protocol.ACK, ackCmd, true);

        SocketAddress address = Mockito.mock(SocketAddress.class);
        UUID token = UUID.randomUUID();
        ByteBuffer buff = getAck(token);
        processor.sessions.put(token, new UdpSession(token, address));

        SocketAddress anotherAddress = Mockito.mock(SocketAddress.class);
        processor.process(anotherAddress, buff);
        verify(handler, never()).handle(address, buff);
        verify(authCmd, never()).handle(address, buff);
        verify(ackCmd, never()).handle(address, buff);

        assertEquals(21, buff.position());
        assertEquals(12, buff.remaining());
    }
    
    @Test
    public void testAuthFailedDefaultCommand() {
        CommandProcessor processor = new CommandProcessor(handler, true);
        SocketAddress address = Mockito.mock(SocketAddress.class);
        ByteBuffer buff = ByteBuffer.allocate(1024);
        UUID uuid = UUID.randomUUID();
        Protocol.auth(buff, uuid);

        processor.process(address, buff);
        verify(handler, never()).handle(address, buff);
    }
    
    
    @Test(expected = IllegalStateException.class)
    public void testAddTwice() throws Exception {
        CommandProcessor processor = new CommandProcessor(null, false);
        CommandHandler authCmd = Mockito.mock(CommandHandler.class);
        processor.add(Protocol.AUTH, authCmd, false);
        processor.add(Protocol.ACK, authCmd, false);
        processor.add(Protocol.AUTH, authCmd, false);
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