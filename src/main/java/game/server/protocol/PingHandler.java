package game.server.protocol;

import game.server.ServerTime;
import game.server.udp.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.UUID;

public class PingHandler implements CommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger(PingHandler.class);
    
    @Autowired
    private DatagramChannel channel;

    @Override
    public void handle(SocketAddress address, ByteBuffer buff, UUID sessionUuid) {
        long time = buff.getLong();
        Protocol.pong(buff, time, ServerTime.mills());
        try {
            channel.send(buff, address);
        } catch (IOException e) {
            LOG.error("[ERR-1005]: Pong sending error", e);
        }    
    }
}
