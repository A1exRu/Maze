package game.server.udp;

import java.net.DatagramSocket;

public class UdpSession {
    
    public static long SESSION_TIMEOUT = 300000;
    
    private final String token;
    private final DatagramSocket socket;
    
    private long timeout;

    public UdpSession(String token, DatagramSocket socket) {
        this.token = token;
        this.socket = socket;
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
