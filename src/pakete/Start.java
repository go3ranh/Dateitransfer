package pakete;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.zip.CRC32;

public class Start extends Paket{
    private byte[] session = new byte[this.sessionLength];
    private String name;
    private long lenght;
    public Start(byte[] session, String name, long length){
        this.buffer = ByteBuffer.allocate(
                sessionLength +
                packetNumberLength +
                "Start".length() +
                8 +//dateilänge
                2 +//dateinamenslänge
                name.length() +
                4
        );
        buffer.order(ByteOrder.BIG_ENDIAN);

        buffer.put(session);
        buffer.put((byte) 0);
        buffer.put("Start".getBytes());
        buffer.putLong(length);
        buffer.putShort((short) name.length());
        buffer.put(name.getBytes());
        CRC32 crc = new CRC32();
        crc.reset();
        crc.update(ByteBuffer.allocate(5 + 8 + 2 + name.length()).put("Start".getBytes()).put(
                buffer.array(),
                sessionLength + packetNumberLength + "Start".length(),
                8 + 2 + name.length()).array());
        buffer.putInt((int) crc.getValue());
    }

    public Start(byte[] paket){
        super(paket);
    }

    public long getFileSize(){
        return buffer.getLong(8);
    }

    public String getName(){
        char nameLen = buffer.getChar(16);
        return new String(Arrays.copyOfRange(buffer.array(), 18, 18 + nameLen));
    }

    public String getKennung(){
        return new String(Arrays.copyOfRange(buffer.array(), 3, 8));
    }
    public int getCRC(){
        return ByteBuffer.wrap(Arrays.copyOfRange(buffer.array(), buffer.array().length - 5, buffer.array().length - 1)).getInt();
    }
}
