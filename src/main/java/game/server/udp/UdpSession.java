package game.server.udp;

import java.net.DatagramSocket;

public class UdpSession {
    
    public static long SESSION_TIMEOUT = 30000;
    
    private final String token;
    private final DatagramSocket socket;
    
    private long aliveTime;

    public UdpSession(String token, DatagramSocket socket) {
        this.token = token;
        this.socket = socket;
        prolong();
    }
    
    public boolean isAlive() {
        return aliveTime > System.currentTimeMillis();
    }
    
    public final void prolong() {
        aliveTime = System.currentTimeMillis() + SESSION_TIMEOUT;
    }

    public String getToken() {
        return token;
    }
}
