package com;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class RWPacket extends Packet{
    byte[] header;

    short optCode;
    String fileName;
    String mode;

    public RWPacket(short optCode, String fileName, String mode) {
        this.optCode = optCode;
        this.fileName = fileName;
        this.mode = mode;
        buildHeader();
    }

    private void buildHeader(){
        ArrayList<Byte> headerTemp = new ArrayList<>();

        headerTemp.add(((byte) 0));
        headerTemp.add(((byte) optCode));

        Byte[] temp = toObjByteArr(fileName.getBytes());
        Collections.addAll(headerTemp, temp);

        headerTemp.add(((byte) 0));

        temp = toObjByteArr(mode.getBytes());
        Collections.addAll(headerTemp, temp);

        headerTemp.add(((byte) 0));

        this.header = toPrimByteArr(headerTemp.toArray());
        System.out.println(Arrays.toString(header));
    }
}
