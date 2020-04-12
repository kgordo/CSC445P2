package packets;

import codes.OPCODES;

import java.nio.ByteBuffer;

public class ACKPacket extends Packet {

    public static final int ACKSIZE = 4;
    short opCode = OPCODES.ACK;
    short blockNum;

    public ACKPacket(short bn){
        blockNum = bn;
        buildHeader();
        encryptDecrypt();
    }

    public ACKPacket(byte[] bytes){
      //  encryptDecrypt(bytes);
        fromBytes(bytes);
    }

    public void buildHeader(){

        ByteBuffer buffer = ByteBuffer.allocate(ACKSIZE);
        buffer.putShort(opCode);
        buffer.putShort(blockNum);

        HEADER = buffer.array();
    }

    public void fromBytes(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, bytes.length);
        opCode = buffer.getShort();
        blockNum = buffer.getShort();

        buildHeader();
    }

    public short getBlockNum(){
        return blockNum;
    }

}
