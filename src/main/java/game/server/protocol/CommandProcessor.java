package game.server.protocol;

import game.server.RequestHandler;
import game.server.udp.Protocol;
import game.server.udp.UdpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(RequestHandler.class);
    private Map<Byte, CommandHandler> handlers = new HashMap<>();
    private final BitSet authRequirements = new BitSet();
    private final CommandHandler defaultHandler;
    private final boolean defaultRequirment;
    final Map<UUID, UdpSession> sessions = new HashMap<>();
    
    public CommandProcessor(CommandHandler defaultHandler, boolean authRequirements) {
        this.defaultHandler = defaultHandler;
        this.defaultRequirment = authRequirements;
        
    }

    public void add(byte cmd, CommandHandler handler, boolean authRequired) {
        if (handlers.containsKey(cmd)) {
            LOG.error("Handler {} has already exists", handler.getClass().getName());
            throw new IllegalStateException("Handler has already exists");
        }
        
        handlers.put(cmd, handler);
        authRequirements.set(cmd, authRequired);
    }
    
    public void process(SocketAddress address, ByteBuffer buff) {
        byte command = Protocol.getCommand(buff);
        boolean supported = handlers.containsKey(command);
        boolean authRequired = supported ? authRequirements.get(command) : defaultRequirment;

        if (authRequired && !validate(address, buff)) {
            return;
        }
        
        CommandHandler handler = supported ? handlers.get(command) : defaultHandler;
        handler.handle(address, buff);
    }
    
    private boolean validate(SocketAddress address, ByteBuffer buff) {
        UUID token = Protocol.getToken(buff);
        UdpSession session = sessions.get(token);
        if (session == null) {
            LOG.warn("Session not found");
            return false;
        }
        
        if (!address.equals(session.getAddress())) {
            LOG.error("Token received from another address");
            return false;
        }
        
//        if (!session.isAlive()) {
//            LOG.warn("Session expired");
//        }
        
        return true;
    }
    
}
