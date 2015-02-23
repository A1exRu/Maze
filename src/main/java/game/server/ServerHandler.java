package game.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ServerHandler implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ServerHandler.class);

    private boolean terminated;
    
    public void onStart() {
           
    }
    
    public abstract void handle() throws Exception;

    @Override
    public void run() {
        onStart();
        while (!terminated) {
            try {
                handle();
            } catch (Exception e) {
                LOG.error("[ERR-1006]: Handling failed", e);
            }
        }
    }

    public void stop() {
        terminated = true;
    }
}
