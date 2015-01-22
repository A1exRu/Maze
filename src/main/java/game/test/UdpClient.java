package game.test;

import game.server.udp.Protocol;
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
    
    private ByteBuffer in = ByteBuffer.allocate(1024);
    private ByteBuffer out = ByteBuffer.allocate(1024);

    private DatagramChannel channel;
    private Selector selector;
    
    private boolean auth;
    
    private volatile String message;
    private volatile Protocol type;
    
    private long pingTime;
    
    public UdpClient(String host, int port) {
        this.address = new InetSocketAddress(host, port);
        this.port = port;

        try {
            selector = Selector.open();
            channel = DatagramChannel.open();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        } catch (Exception e) {
            logger.error("Failed to start client", e);
            terminated = true;
        }
    }
    
    public void start() {
        new Thread(() -> loop(this::receive)).start();
        new Thread(() -> loop(this::transmit)).start();

        Scanner scanner = new Scanner(System.in);
        stop: while (true) {
            String next = scanner.nextLine();
            switch (next) {
                case ":q" : break stop;
                case ":ping": {
                    message = "";
                    type = Protocol.PING;
                    break;
                }
                default: {
                    message = next;
                    type = Protocol.ACK;
                }
            }           
        }
        
        terminated = true;
    }
    
    public void receive() {
        try {
            selector.select(3000);
            Set<SelectionKey> keys = selector.keys();
            for (SelectionKey key : keys) {
                if (key.isReadable()) {
                    in.clear();
                    SocketAddress receive = channel.receive(in);
                    in.flip();
                    int code = in.getInt();
                    Protocol op = Protocol.valueOf(code);
                    if (op == Protocol.PING) {
                        long now = System.currentTimeMillis();
                        System.out.println(now - pingTime);
                        pingTime = -1;
                        in.clear();
                    } else {
                        byte[] datagram = new byte[in.remaining()];
                        in.get(datagram);
                        System.out.print(new String(datagram));
                        in.clear();    
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void transmit() {
        if (message != null && type != null) {
            out.clear();
            switch (type) {
                case PING: {
                    pingTime = System.currentTimeMillis();
                    out.putInt(type.ordinal());
                    break;
                }
                default: {
                    if (auth) {
                        out.putInt(1);
                    } else {
                        out.putInt(0);
                        auth = true;
                    }
                    out.putInt(1);
                    out.put(message.getBytes());
                }
            }
            send();
        }
    }

    private void send() {
        out.flip();
        try {
            channel.send(out, address);
            logger.info("Packet transmitted");
        } catch (IOException e) {
            logger.error("Transmit error", e);
            return;
        } finally {
            message = null;
            type = null;
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
//        socket.close();
    }
}
