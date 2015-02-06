package game.core;

import java.nio.ByteBuffer;

public interface FMarshaller<T extends FUpdate> {

    T apply(ByteBuffer buff);

}
