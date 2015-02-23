package game.bubble;

import game.test.UdpClient;

import java.util.UUID;

public class Context {

    public static final UdpClient udpClient = new UdpClient("localhost", 9187);
    public static String host;
    public static int port;
    public static final UUID authToken = UUID.randomUUID();


    
}
