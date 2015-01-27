package game.server.udp;

import java.nio.ByteBuffer;

public class Protocol {
    
    public static final byte AUTH = 1;
    public static final byte LOGOUT = 2;
    public static final byte REFRESH = 3;
    public static final byte ACK = 4;
    public static final byte NACK = 5;
    public static final byte PACKAGE = 6;
    public static final byte FINAL_PACKAGE = 7;
    public static final byte PING = 8;
    public static final byte PONG = 9;

    public static final int VERSION = 1;

    public static byte[] toDatagram(ByteBuffer buff) {
        int v = buff.getInt();
        if (v != VERSION) {
            throw new IllegalArgumentException("Protocol version not supported");
        }
        
        byte[] bytes = new byte[buff.remaining()];
        buff.get(bytes);
        buff.clear();
        return bytes;
    }
    
    public static void ping(ByteBuffer buff, long time) {
        buff.clear();
        buff.put(PING);
        buff.putInt(VERSION);
        buff.putLong(time);
        buff.flip();
    }

    public static void pong(ByteBuffer buff, long ping, long time) {
        buff.clear();
        buff.put(PONG);
        buff.putInt(VERSION);
        buff.putLong(ping);
        buff.putLong(time);
        buff.flip();
    }
    
    public static void write(ByteBuffer buff, byte ackCmd, byte ackNum, byte[] message) {
        buff.clear();
        buff.put(ackCmd);
        buff.putInt(VERSION);
        buff.putInt(ackNum);
        buff.put(message);
        buff.flip();
    }

}
