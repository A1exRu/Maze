package game.server;

/**
 * @author Alexander
 */
public class Game implements Runnable {

    private final int TICK_SCHEDULE = 120;
    private final int TICK_LENGTH = 30;

    private final long gameId;
    private boolean gameOver;

    private int tick;
    private long lastTime = Long.MAX_VALUE;

    public Game(long gameId) {
        this.gameId = gameId;
    }

    @Override
    public void run() {
        if (gameOver) {
            throw new GameOverException();
        }

        long start = System.currentTimeMillis();
        if (start - lastTime > TICK_SCHEDULE) {
            System.out.println("Scheduler too slow");
        }
        lastTime = start;

        try {
            tick();
        } catch (Exception e) {
            System.out.println("Error detected");
        }

        tick++;

        long end = System.currentTimeMillis();
        if (end - start > TICK_LENGTH) {
            System.out.println("Tick too slow");
        }
    }

    private void tick() {

    }

    public void stop() {
        gameOver = true;
    }

    public long getGameId() {
        return gameId;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getTick() {
        return tick;
    }
}
