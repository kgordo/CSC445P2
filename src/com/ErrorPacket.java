package com;

import java.nio.ByteBuffer;

public class ErrorPacket {

    static final int FIXEDHEADERSIZE = 5;
    static final byte ZEROBYTE = 0;
    private int size;
    short opCode = OPCODES.ERROR;
    short errorCode;
    String errorMsg;
    byte[] header;

    public ErrorPacket(short ec){
        errorCode = ec;
        errorMsg = ERRORCODES.ERRORMESSAGES.get(ec);
        size = FIXEDHEADERSIZE + errorMsg.getBytes().length;
        header = buildHeader();
    }

    byte[] buildHeader(){
        ByteBuffer buffer = ByteBuffer.allocate(size);

        buffer.putShort(opCode);
        buffer.putShort(errorCode);
        buffer.put(errorMsg.getBytes());
        buffer.put(ZEROBYTE);
        return buffer.array();
    }
}
