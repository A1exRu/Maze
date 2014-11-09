package game.world;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;

public class PointTest {

    @Test
    public void testAdd() {
        Point point = new Point(3, 3);
        Point newPoint = point.add(1, 2);
        assertFalse("Point is immutable object, pointers have to be different", point == newPoint);
        assertEquals(3, point.x);
        assertEquals(3, point.y);
        assertEquals(4, newPoint.x);
        assertEquals(5, newPoint.y);
    }

}