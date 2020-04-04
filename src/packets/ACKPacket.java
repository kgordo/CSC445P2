package packets;

import codes.OPCODES;

import java.nio.ByteBuffer;

public class ACKPacket extends Packet {

    private static final int ACKSIZE = 4;
    short optCode = OPCODES.ACK;
    int blockNum;

    public ACKPacket(int bn){
        blockNum = bn;
        buildHeader();

    }

    public void buildHeader(){

        ByteBuffer buffer = ByteBuffer.allocate(ACKSIZE);
        buffer.putShort(optCode);
        buffer.putInt(blockNum);

        HEADER = buffer.array();
    }




}
