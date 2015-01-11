package game.server;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ServerContextTest {

    private final long GAME_ID = 1L;
    private ServerContext context;
    private Game game;

    @Before
    public void setup() {
        context = new ServerContext();
        game = new Game(GAME_ID);
    }

    @Test
    public void tick() throws Exception{
        context.add(game);

        Thread.sleep(210);
        assertEquals(2, game.getTick());

        context.stop(GAME_ID);
        Thread.sleep(100);
        assertEquals(2, game.getTick());
        assertTrue(game.isGameOver());
    }

    @Test
    public void terminate() throws Exception {
        boolean added = context.add(game);
        assertTrue(added);
        Thread.sleep(210);
        assertEquals(2, game.getTick());

        context.terminate();

        Thread.sleep(200);
        assertEquals(2, game.getTick());
        assertTrue(context.isTerminated());

        Game game = new Game(2L);
        boolean added2 = context.add(game);
        assertFalse("Terminated server can't support game", added2);
        Thread.sleep(200);
        assertEquals(0, game.getTick());
    }

}