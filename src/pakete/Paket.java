package pakete;

import java.nio.ByteBuffer;

public class Paket {
    protected int sessionLength = 2;
    protected int crcLength = 4;
    protected int packetNumberLength = 1;

    protected ByteBuffer buffer;

    public Paket(byte[] paket){
        this.buffer = ByteBuffer.wrap(paket);
    }

    public Paket(){

    }

    public byte[] toBytes(){
        return buffer.array();
    }

    public short getSession(){
        return buffer.getShort(0);
    }

    public int getNumber(){
        return (int) buffer.get(3);
    }

}
