package packets;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class Packet {

    static final byte ZEROBYTE = 0;
    byte[] HEADER;

    public DatagramPacket buildPacket(InetAddress host, int port){
        return new DatagramPacket(HEADER, HEADER.length, host, port);
    }
}
