package game.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ServerHandler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);

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
                logger.error("Receiver error", e);
            }
        }
    }

    public void stop() {
        terminated = true;
    }
}
