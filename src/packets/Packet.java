package packets;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class Packet {

    static final byte ZEROBYTE = 0;
    byte[] header;

    public DatagramPacket buildPacket(InetAddress host, int port){
        return new DatagramPacket(header, header.length, host, port);
    }
}
