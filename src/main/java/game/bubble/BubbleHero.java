package game.bubble;

import game.core.Unit;
import game.world.Point;

public class BubbleHero extends Unit {

    public final int color;
    private Point position;
    private float speed;
    
    public BubbleHero(long id, int color, Point startPoint) {
        super(id);
        this.color = color;
        this.position = startPoint;
    }

    @Override
    public Point getPosition() {
        return position;
    }

    @Override
    public void setPosition(Point position) {
        this.position = position;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
