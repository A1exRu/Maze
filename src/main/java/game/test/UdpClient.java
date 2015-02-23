package game.test;

import game.bubble.Context;
import game.server.udp.Packet;
import game.server.udp.Protocol;
import game.test.client.MessageHandler;
import game.test.client.MessageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class UdpClient {

    private static final Logger logger = LoggerFactory.getLogger(UdpClient.class);
    
    private InetSocketAddress address;
    private int port;
    
    private MessageManager manager = new MessageManager();

    private boolean terminated;
    
    private ByteBuffer in = ByteBuffer.allocate(1024);
    private ByteBuffer out = ByteBuffer.allocate(1024);

    private DatagramChannel channel;
    private Selector selector;

    private final Map<Long, Pack> packets = new HashMap<>();

    private final Map<Long, PingCallback> pingMap = new HashMap<>();
    private final Queue<Long> pingQueue = new ArrayBlockingQueue<>(4);

    private final Queue<Word> words = new ArrayBlockingQueue<>(100);

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
    }
    
    public void receive() {
        try {
            selector.select(3000);
            Set<SelectionKey> keys = selector.keys();
            for (SelectionKey key : keys) {
                if (key.isReadable()) {
                    in.clear();
                    channel.receive(in);
                    in.flip();
                    byte cmd = Protocol.getCommand(in);
                    if (cmd == Protocol.PONG) {
                        long ping = in.getLong();
                        long pong = in.getLong();
                        PingCallback pingCallback = pingMap.get(ping);
                        if (pingCallback != null) {
                            pingCallback.call(ping, pong);
                            pingMap.remove(ping);
                        }
                    } else {
                        parseMessage(cmd);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseMessage(byte cmd) {
        final long packetId = in.getLong();
        int num = in.getInt();
        Pack pack = packets.get(packetId);
        if (pack == null) {
            pack = new Pack();
            pack.id = packetId;
            packets.put(packetId, pack);
        }

        byte[] datagram = new byte[in.remaining()];
        in.get(datagram);
        pack.put(num, datagram);
        if (cmd == Protocol.FINAL_PACKAGE) {
            pack.last = num;
        }

        words.add(buff -> Protocol.ack(buff, Context.authToken, packetId, num));
        in.clear();
        if (pack.isReceive()) {
            manager.onMessage(pack.toDatagram());
            packets.remove(packetId);
        }
    }

    public void transmit() {
        while (!pingQueue.isEmpty()) {
            Long time = pingQueue.poll();
            Protocol.ping(out, time);
            send();
        }

        while (!words.isEmpty()) {
            Word word = words.poll();
            word.tell(out);
            send();
        }
    }

    public void ping(PingCallback callback) {
        long now = System.currentTimeMillis();
        pingMap.put(now, callback);
        pingQueue.add(now);
    }

    public void send(Word word) {
        words.add(word);
    }
    
    public void send(byte[] data) {
        words.add(buff -> Protocol.send(buff, Context.authToken, data));
    }
    
    private void send() {
        try {
            channel.send(out, address);
            logger.info("Packet transmitted");
        } catch (IOException e) {
            logger.error("Transmit error", e);
        }
    }

    private void loop(Runnable task) {
        while (!terminated) {
            try {
                task.run();
            } catch (Exception e) {
                logger.error("Error on looping thread");
            }
        }
        
    }

    public void addMessageHandler(MessageHandler handler) {
        manager.register(handler);
    }
    
    public void close() {
        terminated = true;
    }

    public class Pack {
        private long id;
        private Map<Integer, byte[]> data = new TreeMap<>();

        int length;
        int last;

        byte[] toDatagram() {
            if (data.size() - 1 == last){
                byte[] datagram = new byte[length];
                int from = 0;
                for (byte[] bytes : data.values()) {
                    for (int i = 0; i < bytes.length; i++, from++) {
                        datagram[from] = bytes[i];
                    }
                }
                return datagram;
            }

            return Packet.EMPTY;
        }

        void put(int num, byte[] bytes) {
            if (!data.containsKey(num)) {
                data.put(num, bytes);
                length += bytes.length;
            }
        }
        
        boolean isReceive() {
            return (data.size() - 1) == last;
            
        }
    }
    
    public static interface PingCallback {
        void call(long ping, long pong);
    }
    
    public static interface Word {
        void tell(ByteBuffer out);
    }
    
}
