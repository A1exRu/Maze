package game.test;

public class UdpClientRunner {

    public static void main(String[] args) {
        UdpClient client = new UdpClient("localhost", 9187);
        client.start();
        
    }
}
