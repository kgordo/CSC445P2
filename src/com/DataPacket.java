package com;

import javax.swing.plaf.basic.BasicTextAreaUI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class DataPacket extends Packet {

    byte[] header;

    short optCode = OPCODES.DATA;
    short blockNum;
    byte[] data;
    int size;

    public DataPacket (short blockNum, byte[] data){
        this.data = data;
        this.blockNum = blockNum;
        size = 2 + 2 + data.length;
        this.buildHeader();
    }

    private void buildHeader(){

        ByteBuffer buffer = ByteBuffer.allocate(size);

        buffer.putShort(optCode);
        buffer.putShort(blockNum);
        buffer.put(data);

        header = buffer.array();

        //maybe logic to get rid of packets and trow error if they exceed 512B
        System.out.println(Arrays.toString(header));
    }
}
