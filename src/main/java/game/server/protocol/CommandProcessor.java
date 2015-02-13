package game.server.protocol;

import game.server.udp.Protocol;
import game.server.udp.SessionsHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.*;

public class CommandProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CommandProcessor.class);
    
    private Map<Byte, CommandHandler> handlers = new HashMap<>();
    private final BitSet authRequirements = new BitSet();
    
    private final CommandHandler defaultHandler;
    private final boolean defaultRequirement;
    
    private final SessionsHolder sessionsHolder;

    public CommandProcessor(SessionsHolder sessionsHolder, CommandHandler defaultHandler, boolean authRequirements) {
        this.sessionsHolder = sessionsHolder;
        this.defaultHandler = defaultHandler;
        this.defaultRequirement = authRequirements;
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
        boolean authRequired = supported ? authRequirements.get(command) : defaultRequirement;

        UUID sessionUuid = null;
        if (authRequired) {
            sessionUuid = Protocol.getToken(buff);
            if (!sessionsHolder.checkSession(address, sessionUuid)) {
                return;
            }
        }
        
        CommandHandler handler = supported ? handlers.get(command) : defaultHandler;
        handler.handle(address, buff, sessionUuid);
    }
    
    public void validate() {
        Collection<CommandHandler> values = handlers.values();
        long count = values.stream().map(handler -> handler.getClass()).distinct().count();
        if (values.size() != count) {
            throw new IllegalStateException("Same handlers mapped for different commands");
        }
    }
}
