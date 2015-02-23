package game;

import game.bubble.BubbleClient;

import java.io.IOException;

public class RunAll {

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                UdpServerRunner.main(args);
            } catch (IOException e) {
                System.out.println("Server starup failed");
            }
        }).start();
        new Thread(() -> BubbleClient.main(args)).start();
    }
}
