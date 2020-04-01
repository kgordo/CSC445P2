package com;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class DataPacket extends Packet {

    byte[] header;

    short optCode = OPCODES.DATA;
    int blockNum;
    byte[] data;

    public DataPacket (int blockNum, byte[] data){
        this.data = data;
        this.blockNum = blockNum;
        this.buildHeader();
    }

    private void buildHeader(){
        ArrayList<Byte> headerTemp = new ArrayList<>();

        headerTemp.add(((byte) 0));
        headerTemp.add(((byte) optCode));

        Byte[] temp = toObjByteArr(ByteBuffer.allocate(4).putInt(blockNum).array());
        temp = Arrays.copyOfRange(temp,2,  temp.length);

        Collections.addAll(headerTemp, temp);

        temp = toObjByteArr(data);
        Collections.addAll(headerTemp, temp);

        this.header = toPrimByteArr(headerTemp.toArray());
        //maybe logic to get rid of packets and trow error if they exceed 512B
        System.out.println(Arrays.toString(header));
    }
}
