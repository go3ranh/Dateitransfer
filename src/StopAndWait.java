import pakete.Ack;
import pakete.Paket;

import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.HexFormat;

public class StopAndWait implements ARQ{

    private Socket socket;

    public StopAndWait(Socket socket){
        this.socket=socket;
    }
    @Override
    public boolean data_req(byte[] hlData, int hlSize, boolean lastTransmission) {
        int retries = 0;
        boolean success = false;
        Paket send = new Paket(hlData);
        try {
            do {
                retries++;
                socket.sendPacket(hlData);
                Ack ack = new Ack(socket.receivePacket().getData());
                if(ack.getNumber() == send.getNumber() && ack.getSession() == send.getSession()){
                    return true;
                }
            }while (!success && retries < 10);

        }catch (TimeoutException e){
            System.out.println("timeout exception");
        }
        return false;
    }

    @Override
    public byte[] data_ind_req(int remaining) throws TimeoutException {

        DatagramPacket datagramPacket = socket.receivePacket();
        byte[] data = datagramPacket.getData();
        System.out.println(HexFormat.of().formatHex(data, 0, data.length));
        Ack acknowledgePacket = new Ack(Arrays.copyOfRange(data, 0, 2), data[2], (byte) 1);
        socket.sendPacket(acknowledgePacket.toBytes());
        return Arrays.copyOfRange(datagramPacket.getData(), 0, Math.min(this.getMTU(), remaining) );
        //DatagramPacket packet = socket.receivePacket();
        //return packet.getData();
    }

    @Override
    public int getBackData() {
        return 0;
    }

    @Override
    public void closeConnection() {

    }

    @Override
    public int getRetransmissionCounter() {
        return 0;
    }

    @Override
    public int getRetryCounterStat() {
        return 0;
    }

    @Override
    public int getMTU() {
        return 0;
    }

    @Override
    public void setMTU(int MTU) {

    }
}
