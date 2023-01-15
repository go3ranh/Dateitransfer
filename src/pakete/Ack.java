package pakete;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class Ack extends Paket {
    public Ack(byte[] session, byte packetNumber, byte packetCount) {
        buffer = ByteBuffer.allocate(8)
                .put(session)
                .put(packetNumber)
                .put(packetCount)
                .putInt((int) new CRC32().getValue());
    }

    public Ack(byte[] paket) {
        super(paket);
    }
}
