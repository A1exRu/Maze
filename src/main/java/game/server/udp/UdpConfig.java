package game.server.udp;

import org.springframework.beans.factory.annotation.Value;

public class UdpConfig {

    @Value("${udp.transmitter.ack}")
    private boolean ackRequirements;

    @Value("${udp.transmitter.attempts}")
    private int attempts;

    @Value("${udp.transmitter.gc}")
    private int gc;

    @Value("${udp.transmitter.threshold}")
    private int threshold;

    @Value("${udp.transmitter.buff}")
    private int outBufferSize;

    @Value("${udp.transmitter.queue}")
    private int queueSize;

    @Value("${udp.transmitter.packets}")
    private int packetsSize;

    @Value("${udp.packet.maxSize}")
    private int packetMaxSize;

    @Value("${udp.packet.await}")
    private int packetAckAwait;

    public boolean isAckRequirements() {
        return ackRequirements;
    }

    public int getAttempts() {
        return attempts;
    }

    public int getGc() {
        return gc;
    }

    public int getThreshold() {
        return threshold;
    }

    public int getOutBufferSize() {
        return outBufferSize;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public int getPacketsSize() {
        return packetsSize;
    }

    public int getPacketMaxSize() {
        return packetMaxSize;
    }

    public int getPacketAckAwait() {
        return packetAckAwait;
    }
}
