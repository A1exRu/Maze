package game.server.udp;

import game.server.ServerHandler;
import game.server.protocol.CommandProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;

public class Receiver extends ServerHandler {

    private static final Logger LOG = LoggerFactory.getLogger(Receiver.class);

    private final ByteBuffer buff = ByteBuffer.allocate(4096);
    private Selector selector;

    private DatagramChannel channel;
    
    @Autowired
    private CommandProcessor processor;
    
    @Autowired
    private SessionsHolder sessions;
    
    public Receiver(DatagramChannel channel) {
        this.channel = channel;
    }

    @Override
    public void onStart() {
        try {
            selector = Selector.open();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            LOG.error("[ERR-1003]: Initialization of datagram channel and selector error", e);
        }
    }

    @Override
    public void onStop() {
        selector.wakeup();
    }

    @Override
    public void handle() throws IOException {
        sessions.invalidate();
        receive();
    }

    public void receive() throws IOException {
        selector.select();
        Set<SelectionKey> keys = selector.keys();
        for (SelectionKey key : keys) {
            if (key.isReadable()) {
                DatagramChannel ch = (DatagramChannel) key.channel();
                buff.clear();
                SocketAddress address = ch.receive(buff);
                buff.flip();
                processor.process(address, buff);
            }
        }
    }

}
