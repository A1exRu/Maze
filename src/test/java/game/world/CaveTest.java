package game.world;

import game.core.Unit;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Alexander
 */
public class CaveTest {

    private Unit unit;
    private Cave cave;

    @Before
    public void setup() {
        CaveBuilder builder = new CaveBuilder(20, 20)
                .addObstaclesVertical(new Point(9, 5), 15)
                .addObstaclesVertical(new Point(11, 5), 15)
                .addObstaclesHorizontal(new Point(10, 9));
        cave = builder.build();

        unit = new Unit(1l);
        unit.setPosition(new Point(10, 10));
        cave.join(unit);
    }

    @Test
    public void move() {
        Point startPosition = unit.getPosition();
        cave.move(unit, Direction.LEFT);
        assertEquals(startPosition, unit.getPosition());

        cave.move(unit, Direction.RIGHT);
        assertEquals(startPosition, unit.getPosition());

        cave.move(unit, Direction.UP);
        assertEquals(10f, unit.getPosition().x);
        assertEquals(11f, unit.getPosition().y);

        cave.move(unit, Direction.DOWN);
        Point newPosition = unit.getPosition();
        assertEquals(10f, newPosition.x);
        assertEquals(10f, newPosition.y);

        cave.move(unit, Direction.DOWN);
        assertEquals(newPosition, unit.getPosition());
        cave.visualize();
    }
}
