package game.world;

import java.util.*;

/**
 * @author Alexander
 */
public class CaveBuilder {

    public final float width;
    public final float height;

    final Map<Point, Spot> obstacles;

    public CaveBuilder(int width, int height) {
        this.width = width;
        this.height = height;

        obstacles = new HashMap<>(width * height);
    }

    public Cave build() {
        return new Cave(this);
    }

    public CaveBuilder addObstaclesHorizontal(Point obstacle) {
        return addObstacles(obstacle, obstacle.x, obstacle.x, 1, 0);
    }

    public CaveBuilder addObstaclesHorizontal(Point from, int to) {
        return addObstacles(from, from.x, to, 1, 0);
    }

    public CaveBuilder addObstaclesVertical(Point obstacle) {
        return addObstacles(obstacle, obstacle.y, obstacle.y, 0, 1);
    }

    public CaveBuilder addObstaclesVertical(Point from, float to) {
        return addObstacles(from, from.y, to, 0, 1);
    }

    private CaveBuilder addObstacles(Point point, float from, float to, float dx, float dy) {
        float count = Math.abs(from - to);
        int k = from > to ? -1 : 1;
        Point prev = point;
        for (int i = 0; i <= count; i++) {
            Point next = prev.add(i * k * dx, i * k * dy);
            obstacles.putIfAbsent(next, new Spot());
        }

        return this;
    }

}
