package game.world;

import game.core.Unit;
import game.core.event.Event;
import game.scan.service.EventServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Alexander on 01.11.2014.
 */
public class Cave {
    
    public final float width;
    public final float height;

    private Map<Point, Spot> obstacles;

    private List<Unit> units;

    Cave(CaveBuilder builder) {
        this.width = builder.width;
        this.height = builder.height;
        this.units = new ArrayList<>();
        this.obstacles = new HashMap<>(builder.obstacles);
    }

    public void move(Unit unit,  Direction direction) {
        if (unit == null || direction == null) {
            return;
        }

        Point targetPoint = direction.toPoint(unit.getPosition());
        move(unit, targetPoint);
    }

    private void move(Unit unit, Point targetPoint) {
        Spot target = obstacles.get(targetPoint);
        if (target == null) {
            unit.setPosition(targetPoint);
        }

        //TODO: throw event with changes
    }

    public void join(Unit unit) {
        units.add(unit);
        //TODO: throw update event
    }

    public boolean hasObstacle(Point point) {
        return obstacles.containsKey(point);
    }

    public int size() {
        return obstacles.size();
    }

    public void visualize() {
        Map<Point, Unit> unitCache = new HashMap<>();
        units.forEach(unit -> unitCache.put(unit.getPosition(), unit));
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Point point = new Point(j, i);
                if (obstacles.containsKey(point)) {
                    System.out.print('O');
                } else if (unitCache.containsKey(point)) {
                    Unit unit = unitCache.get(point);
                    System.out.print(unit.getId());
                } else {
                    System.out.print('-');
                }
            }
            System.out.println();
        }
    }
}
