package game.core;

import game.world.Point;

public class Unit {

    private final long id;
    private Point position;

    public Unit(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }
}
