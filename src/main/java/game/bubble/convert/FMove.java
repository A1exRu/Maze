package game.bubble.convert;

import java.nio.ByteBuffer;

import game.core.FUpdate;

public class FMove extends FUpdate {
    
    private final double dx;
    private final double dy;

    public FMove(double dx, double dy) {
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public void serialize(ByteBuffer buff) {
        buff.putDouble(dx);
        buff.putDouble(dy);
    }

    @Override
    public void externalize(ByteBuffer buff) {

    }

    public double getDx() {
        return dx;
    }

    public double getDy() {
        return dy;
    }
}
