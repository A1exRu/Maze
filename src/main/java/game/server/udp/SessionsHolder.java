package game.server.udp;

import game.server.ServerTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.*;

public class SessionsHolder {

    private static final Logger LOG = LoggerFactory.getLogger(SessionsHolder.class);

    private final Map<UUID, UdpSession> sessions = new HashMap<>();
    private long threshold = Long.MAX_VALUE;

    public void invalidate() {
        long now = ServerTime.mills();
        if (threshold < now) {
            Set<Map.Entry<UUID, UdpSession>> entries = sessions.entrySet();
            Iterator<Map.Entry<UUID, UdpSession>> it = entries.iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, UdpSession> entry = it.next();
                UdpSession session = entry.getValue();
                if (!session.isAlive()) {
                    it.remove();
                    LOG.debug("Session invalidated {}", session.getId());
                }
            }
        }
    }
    
    public boolean hasSession(UUID token) {
        return sessions.containsKey(token);
    }

    public boolean checkSession(SocketAddress address, UUID sessionUuid) {
        UdpSession session = sessions.get(sessionUuid);
        if (session == null) {
            LOG.warn("[ERR-401]: Session {} not found", sessionUuid);
            return false;
        }

        if (!address.equals(session.getAddress())) {
            LOG.error("[ERR-403]: Token received from another address. Expected: {}, Actual: {}", session.getAddress(), address);
            return false;
        }

        if (!session.isAlive()) {
            LOG.warn("[ERR-401]: Session expired {}", session.getAddress());
            return false;
        }

        session.prolong();
        return true;
    }
    
    public boolean authorize(SocketAddress address, UUID token) {
        boolean valid = validate(token);
        if (valid) {
            UdpSession session = new UdpSession(token, address);
            sessions.put(token, session);  
            updateThreshold(session);
        }
        
        return valid;
    }
    
    public UdpSession get(UUID sessionId) {
        return sessions.get(sessionId);
    }
    
    private boolean validate(UUID token) {
        return token != null;
    }
    
    private void updateThreshold(UdpSession session) {
        threshold = Math.min(threshold, session.getTimeout());
    }

    public long getThreshold() {
        return threshold;
    }
}
