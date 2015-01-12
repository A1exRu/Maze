package game.world;

/**
 * @author Alexander
 */
public enum Direction {

    UP {
        @Override
        public Point toPoint(Point point) {
            return point.add(0, 1);
        }
    },
    DOWN {
        @Override
        public Point toPoint(Point point) {
            return point.add(0, -1);
        }
    },
    LEFT {
        @Override
        public Point toPoint(Point point) {
            return point.add(1, 0);
        }
    },
    RIGHT {
        @Override
        public Point toPoint(Point point) {
            return point.add(-1, 0);
        }
    };

    public Point toPoint(Point point) {
        return point;
    }

}
