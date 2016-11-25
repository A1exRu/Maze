package game;

import java.io.IOException;

import game.bubble.BubbleClient;

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
