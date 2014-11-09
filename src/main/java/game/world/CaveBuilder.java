package game.world;

import java.util.*;

/**
 * Created by Alexander on 01.11.2014.
 */
public class CaveBuilder {

    public final int width;
    public final int height;

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

    public CaveBuilder addObstaclesVertical(Point from, int to) {
        return addObstacles(from, from.y, to, 0, 1);
    }

    private CaveBuilder addObstacles(Point point, int from, int to, int dx, int dy) {
        int count = Math.abs(from - to);
        int k = from > to ? -1 : 1;
        Point prev = point;
        for (int i = 0; i <= count; i++) {
            Point next = prev.add(i * k * dx, i * k * dy);
            obstacles.putIfAbsent(next, new Spot());
        }

        return this;
    }

}
