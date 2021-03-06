package game.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Game implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Game.class);
    
    private final int TICK_SCHEDULE = 120;
    private final int TICK_LENGTH = 30;

    private final long gameId;
    private boolean gameOver;

    private int tick;
    private long lastTime = Long.MAX_VALUE;
    
    private BlockingQueue<Object> updates = new ArrayBlockingQueue(1024);

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
            LOG.warn("Schedule too slow: mills {}, gameId {}", start - lastTime, gameId);
        }
        lastTime = start;

        try {
            tick();
        } catch (Exception e) {
            LOG.error("Tick exception", e);
        }

        tick++;

        long end = System.currentTimeMillis();
        if (end - start > TICK_LENGTH) {
            LOG.warn("Tick too slow: mills {}, gameId {}", end - start, gameId);
        }
    }

    private void tick() {

    }
    
    private void makeUpdates() {
        Object update;
        while ((update = updates.poll()) != null) {

        }
        
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
