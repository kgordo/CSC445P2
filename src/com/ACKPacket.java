package com;

import java.nio.ByteBuffer;

public class ACKPacket extends Packet {

    private static final int ACKSIZE = 4;
    short optCode = OPCODES.ACK;
    int blockNum;
    byte[] header;

    public ACKPacket(int bn){
        blockNum = bn;
        header = buildHeader();

    }

    public byte[] buildHeader(){

        ByteBuffer buffer = ByteBuffer.allocate(ACKSIZE);
        buffer.putShort(optCode);
        buffer.putInt(blockNum);

        return buffer.array();
    }




}
