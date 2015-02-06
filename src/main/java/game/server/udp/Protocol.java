package game.server.udp;

import java.nio.ByteBuffer;
import java.util.UUID;

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
    
    public static byte getCommand(ByteBuffer buff) {
        byte cmd = buff.get();
        int version = buff.getInt();
        if (version != VERSION) {
            throw new IllegalArgumentException("Protocol version not supported");
        }
        
        return cmd;
    }
    
    public static UUID getToken(ByteBuffer buff) {
        if (buff.remaining() < 16) {
            return null;
        }
        
        long most = buff.getLong();
        long least = buff.getLong();
        return new UUID(most, least);
    }

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
    
    public static void auth(ByteBuffer buff, UUID token) {
        buff.clear();
        buff.put(AUTH);
        buff.putInt(VERSION);
        buff.putLong(token.getMostSignificantBits());
        buff.putLong(token.getLeastSignificantBits());
        buff.flip();
    }
    
    public static void write(ByteBuffer buff, long packId, byte cmd, byte num, byte[] message) {
        buff.clear();
        buff.put(cmd);
        buff.putInt(VERSION);
        buff.putLong(packId);
        buff.putInt(num);
        buff.put(message);
        buff.flip();
    }

    public static void send(ByteBuffer buff, byte[] message) {
        buff.clear();
        buff.put(FINAL_PACKAGE);
        buff.putInt(VERSION);
        buff.put(message);
        buff.flip();
    }

}
