package game.server.udp;

import game.bubble.protocol.AuthHandler;
import game.bubble.protocol.MessageHandler;
import game.server.ServerHandler;
import game.server.protocol.AckHandler;
import game.server.protocol.CommandProcessor;
import game.server.protocol.PingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.*;

public class Receiver extends ServerHandler {

    private static final Logger LOG = LoggerFactory.getLogger(Receiver.class);

    private final ByteBuffer buff = ByteBuffer.allocate(4096);
    private final DatagramChannel channel;
    private Selector selector;
    
    private CommandProcessor processor;
    private SessionsHolder sessions = new SessionsHolder();
    
    public Receiver(DatagramChannel channel, Transmitter transmitter) {
        this.channel = channel;
        processor = new CommandProcessor(sessions, new MessageHandler(sessions));
        processor.add(Protocol.AUTH, new AuthHandler(sessions, transmitter));
        processor.add(Protocol.ACK, new AckHandler(transmitter));
        processor.add(Protocol.PING, new PingHandler(channel));
    }

    @Override
    public void onStart() {
        try {
            selector = Selector.open();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            LOG.error("Initialization error", e);
        }
    }

    @Override
    public void handle() throws IOException {
        sessions.invalidate();
        receive();
    }
    
    public void receive() throws IOException {
        selector.select();
        Set<SelectionKey> keys = selector.keys();
        Iterator<SelectionKey> it = keys.iterator();
        while (it.hasNext()) {
            SelectionKey key = it.next();
            if (key.isReadable()) {
                DatagramChannel ch = (DatagramChannel)key.channel();
                buff.clear();
                SocketAddress address = ch.receive(buff);
                buff.flip();
                processor.process(address, buff);
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        selector.wakeup();
    }
}
