package game.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Scanner;
import java.util.Set;

public class UdpClient {

    private static final Logger logger = LoggerFactory.getLogger(UdpClient.class);
    
    private InetSocketAddress address;
    private int port;

    private boolean terminated;
    
    private DatagramSocket socket;
    private ByteBuffer in = ByteBuffer.allocate(1024);
    private ByteBuffer out = ByteBuffer.allocate(1024);

    private DatagramChannel channel;
    private Selector selector;
    
    private boolean auth;
    private volatile String message;

    public UdpClient(String host, int port) {
        this.address = new InetSocketAddress(host, port);
        this.port = port;

        try {
            socket = new DatagramSocket();
            selector = Selector.open();
            channel = DatagramChannel.open();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
        } catch (Exception e) {
            logger.error("Failed to start client", e);
            terminated = true;
        }
    }
    
    public void start() {
        new Thread(() -> loop(this::receive)).start();
        new Thread(() -> loop(this::transmit)).start();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String next = scanner.nextLine();
            if (":q".equals(next)) {
                break;
            }
            
            message = next;
        }
        
        terminated = true;
    }
    
    public void receive() {
//        byte[] datagram = new byte[1024];
//        DatagramPacket req = new DatagramPacket(datagram, datagram.length, address);
//        try {
//            socket.receive(req);
//        } catch (IOException e) {
//            logger.error("Receive error", e);
//            return;
//        }

        try {
            selector.select(3000);
            Set<SelectionKey> keys = selector.keys();
            for (SelectionKey key : keys) {
                if (key.isReadable()) {
                    SocketAddress receive = channel.receive(in);
                    byte[] datagram = new byte[in.remaining()];
                    System.out.println(new String(datagram));
                    in.clear();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void transmit() {
        if (message != null) {
            out.clear();
            if (auth) {
                out.putInt(1);
            } else {
                out.putInt(0);
                auth = true;
            }
            out.putInt(1);
            out.put(message.getBytes());
            out.flip();
            byte[] data = new byte[out.remaining()];
            out.get(data);
            DatagramPacket packet = new DatagramPacket(data, data.length, address);
            try {
                
                socket.send(packet);
                logger.info("Packet transmitted");
            } catch (IOException e) {
                logger.error("Transmit error", e);    
                return;
            } finally {
                message = null;
            }
        }
    }
    
    private void loop(Runnable task) {
        while (!terminated) {
//            System.out.println(task);
            try {
                task.run();
            } catch (Exception e) {
                logger.error("Error on looping thread");
            }
        }
        
    }
    
    public void close() {
        terminated = true;
        socket.close();
    }
}
