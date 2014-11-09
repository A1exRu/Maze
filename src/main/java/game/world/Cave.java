package game.world;

import game.core.Unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void move(Unit unit, Direction direction) {
        if (unit == null || direction == null) {
            return;
        }
        Point position = unit.getPosition();
        Point targetPoint = direction.toPoint(position);
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

}
