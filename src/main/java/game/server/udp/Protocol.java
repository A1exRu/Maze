package game.server.udp;

import java.nio.ByteBuffer;

public enum Protocol {

    AUTHENTICATION,
    ACK,
    ;
    
    public static final int VERSION = 1;

    public byte[] toDatagram(ByteBuffer buff) {
        int v = buff.getInt();
        if (v != VERSION) {
            throw new IllegalArgumentException("Protocol version not supported");
        }
        
        byte[] bytes = new byte[buff.remaining()];
        buff.get(bytes);
        buff.clear();
        return bytes;
    }
    
    public void write(ByteBuffer buff, byte[] message) {
        buff.clear();
        buff.putInt(ordinal());
        buff.putInt(VERSION);
        buff.put(message);
        buff.flip();
    }
    
    public static Protocol valueOf(int ordinal) {
        if (ordinal >= Protocol.values().length) {
            throw new IllegalArgumentException("Protocol operation not support");
        }
        return values()[ordinal];
    }

}
