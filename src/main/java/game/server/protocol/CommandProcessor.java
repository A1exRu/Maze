package game.server.protocol;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import game.server.udp.Protocol;
import game.server.udp.SessionsHolder;

public class CommandProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CommandProcessor.class);
    
    private CommandHandler defaultHandler;
    private Map<Byte, CommandHandler> handlers = new HashMap<>();

    @Autowired
    private SessionsHolder sessionsHolder;
    
    public void process(SocketAddress address, ByteBuffer buff) {
        byte command = Protocol.getCommand(buff);
        CommandHandler handler = handlers.containsKey(command) ? handlers.get(command) : defaultHandler;

        UUID sessionUuid = null;
        if (handler.isAuthRequired()) {
            sessionUuid = Protocol.getToken(buff);
            if (!sessionsHolder.checkSession(address, sessionUuid)) {
                return;
            }
        }
        
        handler.handle(address, buff, sessionUuid);
    }
    
    public void validate() {
        Collection<CommandHandler> values = handlers.values();
        long count = values.stream().map(handler -> handler.getClass()).distinct().count();
        if (values.size() != count) {
            throw new IllegalStateException("Same handlers mapped for different commands");
        }
    }

    public void add(byte cmd, CommandHandler handler) {
        if (handlers.containsKey(cmd)) {
            LOG.error("[ERR-1100]: Handler {} has already exists", handler.getClass().getName());
            throw new IllegalStateException("Handler has already exists");
        }

        handlers.put(cmd, handler);
    }

    public void setDefaultHandler(CommandHandler defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    public void setHandlers(Map<Byte, CommandHandler> handlers) {
        this.handlers = handlers;
    }
}
