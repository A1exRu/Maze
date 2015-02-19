package game.server;

import org.junit.After;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class ServerTimeTest {

    private UUID key;

    @After
    public void destroy() {
        boolean res = ServerTime.unlockProduction(key);
        if (!res) {
            fail("Production mode not locked");
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testProduction() throws Exception {
        key = ServerTime.lockAsProduction();
        assertNull(ServerTime.lockAsProduction());
        ServerTime.toFixed();
    }

    @Test(expected = IllegalStateException.class)
    public void testAddWhenProduction() throws Exception {
        key = ServerTime.lockAsProduction();
        ServerTime.addMills(100);
    }

    @Test
    public void testUnlockProduction() throws Exception {
        key = ServerTime.lockAsProduction();
        boolean res = ServerTime.unlockProduction(null);
        assertFalse(res);

        res = ServerTime.unlockProduction(key);
        assertTrue(res);
    }

    @Test
    public void testToDefault() throws Exception {
        ServerTime.toFixed();
        long mills = ServerTime.mills();
        assertEquals(0, mills);

        ServerTime.toDefault();
        mills = ServerTime.mills();
        long systemTime = System.currentTimeMillis();
        assertEquals(systemTime, mills, 5);
    }

    @Test
    public void testAddMills() throws Exception {
        ServerTime.toFixed();
        long time = ServerTime.mills();
        assertEquals(0, time);
        ServerTime.addMills(100);
        time = ServerTime.mills();
        assertEquals(100, time);
    }
}