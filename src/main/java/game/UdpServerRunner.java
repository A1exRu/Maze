package game;

import game.server.udp.UdpServer;

import java.io.IOException;
import java.util.Scanner;

public class UdpServerRunner {

    public static void main(String[] args) throws IOException {
        UdpServer server = new UdpServer(9187);
        server.start();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String next = scanner.next();
            if (":q".equals(next)) {
                server.stop();
                break;
            }
        }
    }


}
