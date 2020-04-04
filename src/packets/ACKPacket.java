package packets;

import codes.OPCODES;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class ACKPacket extends Packet {

    private static final int ACKSIZE = 4;
    short optCode = OPCODES.ACK;
    int blockNum;

    public ACKPacket(int bn){
        blockNum = bn;
        buildHeader();

    }

    public ACKPacket(byte[] bytes){
        fromBytes(bytes);
    }

    public void buildHeader(){

        ByteBuffer buffer = ByteBuffer.allocate(ACKSIZE);
        buffer.putShort(optCode);
        buffer.putInt(blockNum);

        HEADER = buffer.array();
    }

    public void fromBytes(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, bytes.length);
        optCode = buffer.getShort();
        blockNum = buffer.getInt();
    }


    public static void main(String[] args) throws UnknownHostException {
        ACKPacket test = new ACKPacket(1);
        //test.buildPacket(InetAddress.getLocalHost(), 88);
        //DatagramPacket test2 = test.getDataGramPacket();
        //byte[] test3 = test2.getData();
        //ACKPacket readTest = new ACKPacket(test3);
        //System.out.println("Read block #: " + readTest.blockNum);

    }



}
