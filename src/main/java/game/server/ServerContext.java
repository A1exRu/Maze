package game.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Alexander
 */
public class ServerContext {

    public static final int TICK_LEN = 100;

    private final Map<Long, Game> games = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler;

    private boolean terminated;

    public ServerContext() {
        scheduler = Executors.newScheduledThreadPool(4);
    }

    public boolean add(Game task) {
        if (terminated) {
            System.out.println("Server was terminated");
        } else {
            games.put(task.getGameId(), task);
            scheduler.scheduleAtFixedRate(task, TICK_LEN, TICK_LEN, TimeUnit.MILLISECONDS);
        }

        return !terminated;
    }

    public void stop(long gameId) {
        Game game = games.get(gameId);
        game.stop();
        games.remove(gameId);
    }

    public void terminate() {
        terminated = true;
        scheduler.shutdown();
        for (Game game : games.values()) {
            game.stop();
        }

        try {
            scheduler.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.out.println("Some games has been frozen");
        }
        games.clear();
    }

    public boolean isTerminated() {
        return terminated;
    }
}
