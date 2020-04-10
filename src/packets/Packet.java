package packets;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class Packet {

    static final byte ZEROBYTE = 0;
    byte[] HEADER;
    DatagramPacket packet;

    public void buildPacket(InetAddress host, int port){
        packet = new DatagramPacket(HEADER, HEADER.length, host, port);
    }

    public DatagramPacket getDataGramPacket(InetAddress host, int port){
        packet = new DatagramPacket(HEADER, HEADER.length, host, port);
        return packet;
    }
}
