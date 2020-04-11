package packets;

import utils.XOR;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class Packet {

    static final byte ZEROBYTE = 0;
    static byte[] HEADER;
    DatagramPacket packet;

    public void buildPacket(InetAddress host, int port){
        packet = new DatagramPacket(HEADER, HEADER.length, host, port);
    }

    public DatagramPacket getDataGramPacket(InetAddress host, int port){
        packet = new DatagramPacket(HEADER, HEADER.length, host, port);
        return packet;
    }

    public void encryptDecrypt(){
        XOR.fullDecode(HEADER);
    }

    public static void encryptDecrypt(byte[] data){
        XOR.fullDecode(data);
    }
}
