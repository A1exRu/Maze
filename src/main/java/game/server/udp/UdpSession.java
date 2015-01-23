package game.server.udp;

import java.net.SocketAddress;

public class UdpSession {
    
    public static long SESSION_TIMEOUT = 300000;
    
    private final String token;
    private final SocketAddress address;
//    private final long gameId;
//    private final long heroId;
    
    private long timeout;

    public UdpSession(String token, SocketAddress address) {
        this.token = token;
        this.address = address;
        prolong();
    }
    
    public boolean isAlive() {
        return timeout > System.currentTimeMillis();
    }
    
    public final long prolong() {
        timeout = System.currentTimeMillis() + SESSION_TIMEOUT;
        return timeout;
    }

    public String getToken() {
        return token;
    }

    public long getTimeout() {
        return timeout;
    }
}
