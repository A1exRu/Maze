package game;

import java.io.IOException;
import java.util.Scanner;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import game.server.udp.UdpServer;

public class UdpServerRunner {

    public static void main(String[] args) throws IOException {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        UdpServer server = ctx.getBean(UdpServer.class);
        server.start();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String next = scanner.next();
            if (":q".equals(next)) {
                server.stop();
                ctx.destroy();
                scanner.close();
                break;
            } else if (":status".equals(next)) {
                System.out.println("Server running");
            }
        }
    }


}
