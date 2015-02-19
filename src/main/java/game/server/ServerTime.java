package game.server;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

public class ServerTime {

    private static Clock clock = Clock.systemDefaultZone();
    private static boolean productionMode;
    private static UUID secretKey;
    
    private ServerTime(){
    }
    
    public static long mills() {
        return clock.millis();
    }
    
    public static UUID lockAsProduction() {
        if (!productionMode) {
            productionMode = true;
            clock = Clock.systemDefaultZone();
            secretKey = UUID.randomUUID();
            return secretKey;
        }

        return null;
    }
    
    public static boolean unlockProduction(UUID secret) {
        if (secretKey != null && secretKey.equals(secret)) {
            productionMode = false;
            secretKey = null;
        }    
        
        return !productionMode;
    }

    public static void toDefault() {
        checkProductionMode();
        clock = Clock.systemDefaultZone();
    }
    
    public static void toFixed() {
        checkProductionMode();
        clock = Clock.fixed(Instant.EPOCH, ZoneId.systemDefault());
    }

    public static long addMills(long mills) {
        checkProductionMode();
        clock = Clock.offset(clock, Duration.ofMillis(mills));
        return mills();
    }

    private static void checkProductionMode() {
        if (productionMode) {
            throw new IllegalStateException("Fixed type not supported");
        }
    }
    
}
