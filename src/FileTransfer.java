import pakete.Ack;
import pakete.Data;
import pakete.Start;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Random;
import java.util.zip.CRC32;

public class FileTransfer implements FT{
    private String host;
    private Socket socket;
    private String fileName;
    private ARQ arq;
    private String dir;
    private byte[] session = new byte[2];
    private byte packetnumber = 1;

    public FileTransfer(String host, Socket socket, String fileName, String arq){
        this.host = host;
        this.socket = socket;
        this.fileName = fileName;
        //todo implement go back n
        this.arq = new StopAndWait(socket);

        this.arq.setMTU(1000);
    }

    public FileTransfer(Socket socket, String dir){
        this.socket = socket;
        this.dir = dir;
        this.arq = new StopAndWait(socket);
    }

    public boolean file_req(){
        System.out.println("file req started");
        File file = new File(fileName);
        try(FileInputStream fis = new FileInputStream(file)){
            new Random().nextBytes(session);
            System.out.println("sessionid: " + ByteBuffer.wrap(session).getShort());

            Start s = new Start(session, fileName, file.length());
            byte[] raw = s.toBytes();
            if (!arq.data_req(raw, raw.length, false)){
                System.out.println("Start packet could not be sent");
                //error - header nicht gesendet
                return false;
            }
            System.out.println("Start packet was sent");

            byte[] data = new byte[arq.getMTU() < file.length() ? arq.getMTU() : (int) file.length()];
            System.out.println("TEST filesize mtu: " + file.length() + " " + arq.getMTU());
            while (fis.read(data) != -1){
                Data dataPacket = new Data(session, packetnumber, data);
                if (!arq.data_req(dataPacket.toBytes(), dataPacket.toBytes().length, false)){
                    System.out.println("Data packet transmission failed" + (int)packetnumber);
                    return false;
                }
                System.out.println("Data packet sent" + (int) packetnumber);
                packetnumber++;
            }
            CRC32 crc = new CRC32();
            crc.update(fis.readAllBytes());
        } catch (IOException ignored) {
        }
        return true;
    }

    public boolean file_init(){
        System.out.println("file init started");
        try {
            DatagramPacket incoming = socket.receivePacket();
            System.out.println("packet from " + incoming.getAddress().getHostAddress());
            Start start = new Start(incoming.getData());

            if (!start.getKennung().equals("Start")){
                System.out.println("Start packet does not contain start at offset 5");
                return false;
            }
            System.out.println("Start packet received");

            long fileSize = start.getFileSize();

            //todo ändern, das ist mist
            session = ByteBuffer.allocate(2).putShort(start.getSession()).array();
            System.out.println("Session number is " + ByteBuffer.wrap(session).getShort());

            Ack ack = new Ack(session, (byte) packetnumber, (byte) 1);
            socket.sendPacket(ack.toBytes());
            packetnumber++;
            //todo byte array oder so machen, weil allocate nur int zulässt und wir extra long nutzen
            ByteBuffer received = ByteBuffer.allocate((int) fileSize);

            long bytesReceived = 0;

            while (bytesReceived < fileSize){
                byte[] packet = arq.data_ind_req((int) (fileSize - bytesReceived));
                System.out.println(HexFormat.of().formatHex(packet, 0, packet.length-1));
                if (
                        ByteBuffer.wrap(Arrays.copyOfRange(packet, 0, 2)).getShort() == ByteBuffer.wrap(session).getShort()
                        && packetnumber == packet[2]
                ){
                    System.out.println("packet is valid" + (int)packetnumber);
                    received.put(Arrays.copyOfRange(packet, 2, packet.length));
                    Ack a = new Ack(session, (byte) packetnumber, (byte) 1);
                    socket.sendPacket(a.toBytes());
                    packetnumber++;
                    bytesReceived += packet.length-3;
                }else {
                    System.out.println("invalid packet");
                    System.out.println("Sessionid: " + ByteBuffer.wrap(packet).getShort());
                    System.out.println("packetid: " + (int)ByteBuffer.wrap(packet).get(2));
                    System.out.println("filesize: " + start.getFileSize());
                }
            }

            //todo final ack + crc
            File safe = new File(dir, start.getName());
            while (safe.exists()){
                safe = new File(dir, safe.getName()+"1");
            }
            try (FileOutputStream fos = new FileOutputStream(safe)){
                fos.write(received.array());
            } catch (IOException ignored) {
            }
        }catch (TimeoutException ignored){
        }
        return true;
    }
}
