package game.world;

/**
 * @author Alexander
 */
public class Point {

    public final float x;
    public final float y;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Point add(float x, float y) {
        return new Point(this.x + x, this.y + y);
    }

    public float distance(Point to) {
        if (to == null) {
            return 0;
        }

        return distance(to.x, to.y);
    }

    public float distance(float x, float y) {
        float a = Math.abs(this.x - x);
        float b = Math.abs(this.y - y);
        return (float)Math.sqrt(a * a + b * b);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;

        if (Float.compare(point.x, x) != 0) return false;
        if (Float.compare(point.y, y) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        return result;
    }
}
