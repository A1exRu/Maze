package game.world;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;

/**
 * @author Alexander
 */
public class PointTest {

    @Test
    public void testAdd() {
        Point point = new Point(3, 3);
        Point newPoint = point.add(1, 2);
        assertFalse("Point is immutable object, pointers have to be different", point == newPoint);
        assertEquals(3f, point.x);
        assertEquals(3f, point.y);
        assertEquals(4f, newPoint.x);
        assertEquals(5f, newPoint.y);
    }

    @Test
    public void testDistance() {
        Point point = new Point(0, 0);
        assertEquals(0f, point.distance(null));
        assertEquals(5.0, point.distance(5, 0), 0.01);
        assertEquals(5.0, point.distance(0, 5), 0.01);
        assertEquals(5.0, point.distance(new Point(3, 4)), 0.01);
    }

}