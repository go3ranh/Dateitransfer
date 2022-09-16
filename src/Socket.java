import static java.net.InetAddress.getByName;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/** @author Jörg Vogt */

public class Socket {
  private static double lossRate = 0.0;
  private static int averageDelay = 0; // milliseconds end-to-end
  private static Random random;
  private static final int MTU_max = 65536; // for receiver
  private static boolean isServer;
  private InetAddress peerAddress;
  private int port;
  private final DatagramSocket socket;
  private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  private static final String RED = "\u001B[31m";
  BlockingQueue queue = new ArrayBlockingQueue(256);
  // http://tutorials.jenkov.com/java-util-concurrent/blockingqueue.html
  ChannelDelay ch =  new ChannelDelay();

  /**
   * Client usage
   * @param host Host to connect
   * @param port Port to connect
   * @throws SocketException in case of problems
   * @throws UnknownHostException as the exception says
   */
  public Socket(String host, int port) throws SocketException, UnknownHostException {
    isServer = false;
    this.port = port;
    setChannelSimulator(0, 0); // dealing with network error only at server side
    peerAddress = getByName(host);
    socket = new DatagramSocket();
    // Linux default Receive buffer: 208 kB -> about 150 packets
    // sysctl -a | grep udp
    // sysctl -w net.core.rmem_max=8388608
    logger.log(Level.FINER, "C: Rcv Buffer Size:" + socket.getReceiveBufferSize());
    ch.start();
  }

  /**
   * Server usage
   * @param port Port for the connection from clients
   * @param loss simulate network loss (0...1)
   * @param delay simulate network delay (average RTT in ms)
   * @throws SocketException in case of socket problems
   */
  public Socket(int port, double loss, int delay) throws SocketException {
    isServer = true;
    setChannelSimulator(loss, delay);
    socket = new DatagramSocket(port);
    logger.log(Level.FINER, "S: Rcv Buffer Size:" + socket.getReceiveBufferSize());
    ch.start();
  }

  public static void setChannelSimulator(double loss, int delay) {
    lossRate = loss;
    averageDelay = delay;
    // Create random number generator for use in simulating packet loss and network delay.
    random = new Random();
    long seed = 1; // TODO remove if not debug
    random.setSeed(seed);
  }

  /**
   * Client/server: sends a packet to fixed destination
   * @param sendData DataPacket to send
   */
  public void sendPacket(byte[] sendData)  {
    DatagramPacket packet = new DatagramPacket(sendData, sendData.length, peerAddress, port);
    if (simulateLoss()) {
      queue.add(packet);
    }
  }

  /**
   * Client: get a packet from Server
   * Server: get a packet from Client and saves address
   * @return Data
   * @throws TimeoutException socket timeout
   */
  public DatagramPacket receivePacket() throws TimeoutException {
    DatagramPacket dataPacket = new DatagramPacket(new byte[MTU_max], MTU_max);
    do {
      try {
        socket.receive(dataPacket);
      } catch (SocketTimeoutException e) {
        throw new TimeoutException();
      } catch (IOException e) {
        e.printStackTrace();
      }

      if (isServer) {
        peerAddress = dataPacket.getAddress();
        port = dataPacket.getPort();
      }
    } while (!simulateLoss());  // no delay on receive path
    return dataPacket;
  }

  /**
   * Sets the timeout for receiving a packet
   * @param timeout the value to set for the receiving socket
   */
  public void setTimeout(int timeout)  {
    try {
      socket.setSoTimeout(timeout);
    } catch (SocketException e) {
      e.printStackTrace();
    }
  }

  /**
   * Simulates a WAN with delay and loss
   * @return true if no loss
   */
  private static boolean simulateLoss()  {
    boolean error = (random.nextDouble() < lossRate);
    if (error) {
      logger.log(Level.FINER, RED + "Channel:    *** Packet lost ***");
    }
    return !error;
  }

  class ChannelDelay extends Thread {
    DatagramPacket packet;

    public void run() {
      do {
        try {
          packet = (DatagramPacket) queue.take(); // blocks
          Thread.sleep((int) (random.nextDouble() * 1.0 * averageDelay + 0.5*averageDelay  ));
          socket.send(packet);
          // send all packets in queue at the same time
          while (!queue.isEmpty()) {
            packet = (DatagramPacket) queue.take();
            socket.send(packet);
          }
        } catch (InterruptedException | IOException e) {
          e.printStackTrace();
        }
      } while (true);
    }
  }
}
