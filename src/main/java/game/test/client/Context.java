package game.test.client;

import game.test.UdpClient;

public class Context {

    public static final UdpClient udpClient = new UdpClient("localhost", 9187);
    public static String host;
    public static int port;
    public static final String authToken = "token_" + System.currentTimeMillis();


    
}
