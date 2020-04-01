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
    int size;

    public RWPacket(short optCode, String fileName, String mode) {
        this.optCode = optCode;
        this.fileName = fileName;
        this.mode = mode;
        this.size = 4 + fileName.getBytes().length + mode.getBytes().length;
        buildHeader();
    }

    private void buildHeader(){

        ByteBuffer buffer = ByteBuffer.allocate(size);

        buffer.putShort(optCode);
        buffer.put(fileName.getBytes());
        buffer.put(ZEROBYTE);
        buffer.put(mode.getBytes());
        buffer.put(ZEROBYTE);

        header = buffer.array();

        System.out.println(Arrays.toString(header));
    }
}
