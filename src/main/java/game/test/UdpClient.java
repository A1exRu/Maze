package game.test;

import game.server.udp.Packet;
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
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class UdpClient {

    private static final Logger logger = LoggerFactory.getLogger(UdpClient.class);
    
    private InetSocketAddress address;
    private int port;

    private boolean terminated;
    
    private ByteBuffer in = ByteBuffer.allocate(1024);
    private ByteBuffer out = ByteBuffer.allocate(1024);

    private DatagramChannel channel;
    private Selector selector;

    private Map<Long, Pack> packets = new HashMap<>();
    private volatile List<Ack> acks = new CopyOnWriteArrayList<>();

    private boolean auth;
    
    private volatile String message;
    private volatile byte type;
    
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
                case ":q" : {
                    close();
                    break stop;
                }
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
                    byte cmd = in.get();
                    int version = in.getInt();
                    if (cmd == Protocol.PING) {
                        long now = System.currentTimeMillis();
                        System.out.println(now - pingTime);
                        pingTime = -1;
                        in.clear();
                    } else {
                        int num = in.getInt();
                        final long TEMP_PACK = 1l;
                        Pack pack = packets.get(TEMP_PACK);
                        if (pack == null) {
                            pack = new Pack();
                            pack.id = TEMP_PACK;
                            packets.put(TEMP_PACK, pack);
                        }

                        byte[] datagram = new byte[in.remaining()];
                        in.get(datagram);
                        pack.put(num, datagram);
                        if (cmd == Protocol.FINAL_PACKAGE) {
                            pack.last = num;
                        }

                        acks.add(new Ack(TEMP_PACK, num));
                        in.clear();    
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void transmit() {
        if (message != null && type != 0) {
            out.clear();
            switch (type) {
                case Protocol.PING: {
                    pingTime = System.currentTimeMillis();
                    out.put(type);
                    break;
                }
                default: {
                    if (!auth) {
                        out.put(Protocol.AUTH);
                    } else {
                        out.put(Protocol.REFRESH);
                        auth = true;
                    }
                    out.putInt(Protocol.VERSION);
                    out.put(message.getBytes());
                }
            }
            send();
        }

        if (acks.size() > 0) {
            for (Ack ack : acks) {
                out.clear();
                out.put(Protocol.ACK);
                //TODO: complete ack transmission
            }
        }
    }

    private void send() {
        out.flip();
        try {
            channel.send(out, address);
            logger.info("Packet transmitted");
        } catch (IOException e) {
            logger.error("Transmit error", e);
        }

        message = null;
        type = 0;
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
    }

    public class Ack {
        long packId;
        int num;

        public Ack(long packId, int num) {
            this.packId = packId;
            this.num = num;
        }
    }
}
