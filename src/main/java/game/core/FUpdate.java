package game.core;

import java.nio.ByteBuffer;

public abstract class FUpdate {

    public abstract void serialize(ByteBuffer buff);

    public abstract void externalize(ByteBuffer buff);
    
}
