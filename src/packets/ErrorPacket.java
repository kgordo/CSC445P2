package packets;

import codes.ERRORCODES;
import codes.OPCODES;

import java.nio.ByteBuffer;

public class ErrorPacket extends Packet {

    static final int FIXEDHEADERSIZE = 5;
    private int size;
    short opCode = OPCODES.ERROR;
    short errorCode;
    String errorMsg;

    public ErrorPacket(short ec){
        errorCode = ec;
        errorMsg = ERRORCODES.ERRORMESSAGES.get(ec);
        size = FIXEDHEADERSIZE + errorMsg.getBytes().length;
        buildHeader();
    }

    private void buildHeader(){

        ByteBuffer buffer = ByteBuffer.allocate(size);

        buffer.putShort(opCode);
        buffer.putShort(errorCode);
        buffer.put(errorMsg.getBytes());
        buffer.put(ZEROBYTE);

        HEADER = buffer.array();
    }

}