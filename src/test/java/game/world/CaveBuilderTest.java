package game.world;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class CaveBuilderTest {

    @Test
    public void testAddLineHorizontal() {
        CaveBuilder builder = new CaveBuilder(20, 20)
                .addObstaclesHorizontal(new Point(10, 10), 15);
        Cave cave = builder.build();
        assertEquals(6, cave.size());
        assertTrue(cave.hasObstacle(new Point(10, 10)));
        assertTrue(cave.hasObstacle(new Point(11, 10)));
        assertTrue(cave.hasObstacle(new Point(12, 10)));
        assertTrue(cave.hasObstacle(new Point(13, 10)));
        assertTrue(cave.hasObstacle(new Point(14, 10)));
        assertTrue(cave.hasObstacle(new Point(15, 10)));
        assertFalse(cave.hasObstacle(new Point(16, 10)));
    }

    @Test
    public void testAddLineVertical() {
        CaveBuilder builder = new CaveBuilder(20, 20)
                .addObstaclesVertical(new Point(10, 10), 15);
        Cave cave = builder.build();
        assertEquals(6, cave.size());
        assertTrue(cave.hasObstacle(new Point(10, 10)));
        assertTrue(cave.hasObstacle(new Point(10, 11)));
        assertTrue(cave.hasObstacle(new Point(10, 12)));
        assertTrue(cave.hasObstacle(new Point(10, 13)));
        assertTrue(cave.hasObstacle(new Point(10, 14)));
        assertTrue(cave.hasObstacle(new Point(10, 15)));
        assertFalse(cave.hasObstacle(new Point(10, 16)));
    }

    @Test
    public void testDuplicates() {
        CaveBuilder builder = new CaveBuilder(20, 20)
                .addObstaclesVertical(new Point(10, 10), 15);

        Cave cave = builder.build();
        assertEquals(6, cave.size());

        cave = builder.addObstaclesVertical(new Point(10, 19), 15).build();
        assertEquals(10, cave.size());

        cave = builder.addObstaclesVertical(new Point(10, 19), 10).build();
        assertEquals(10, cave.size());
    }

}