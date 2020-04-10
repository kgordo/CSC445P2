package packets;

import codes.OPCODES;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class ACKPacket extends Packet {

    private static final int ACKSIZE = 4;
    short optCode = OPCODES.ACK;
    short blockNum;

    public ACKPacket(short bn){
        blockNum = bn;
        buildHeader();

    }

    public ACKPacket(byte[] bytes){
        fromBytes(bytes);
    }

    public void buildHeader(){

        ByteBuffer buffer = ByteBuffer.allocate(ACKSIZE);
        buffer.putShort(optCode);
        buffer.putShort(blockNum);

        HEADER = buffer.array();
    }

    public void fromBytes(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, bytes.length);
        optCode = buffer.getShort();
        blockNum = buffer.getShort();
    }

    public short getBlockNum(){
        return blockNum;
    }

}
