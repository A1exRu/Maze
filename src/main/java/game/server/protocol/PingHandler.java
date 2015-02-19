package game.server.protocol;

import game.server.ServerTime;
import game.server.udp.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.UUID;

public class PingHandler implements CommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger(PingHandler.class);
    private final DatagramChannel channel;

    public PingHandler(DatagramChannel channel) {
        this.channel = channel;
    }

    @Override
    public void handle(SocketAddress address, ByteBuffer buff, UUID sessionUuid) {
        long time = buff.getLong();
        Protocol.pong(buff, time, ServerTime.mills());
        try {
            channel.send(buff, address);
        } catch (IOException e) {
            LOG.error("Send ping error", e);
        }    
    }
}
