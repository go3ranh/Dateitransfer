package pakete;

import java.nio.ByteBuffer;

public class Data extends Paket{

    public Data(byte[] session, byte pNumber, byte[] data){
        buffer = ByteBuffer.allocate(3+ data.length);
        buffer.put(session);
        buffer.put(pNumber);
        buffer.put(data);
    }
}
