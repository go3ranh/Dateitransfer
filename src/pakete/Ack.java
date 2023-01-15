package pakete;

import java.nio.ByteBuffer;

public class Ack extends Paket{
    public Ack(byte[] session, byte packetNumber, byte packetCount){
        buffer = ByteBuffer.allocate(4);
        buffer.put(session);
        buffer.put(packetNumber);
        buffer.put(packetCount);
    }
    public Ack(byte[] paket) {
        super(paket);
    }
}
