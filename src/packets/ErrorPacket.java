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

    public ErrorPacket(byte[] bytes){
        fromBytes(bytes);
    }

    private void buildHeader(){

        ByteBuffer buffer = ByteBuffer.allocate(size);

        buffer.putShort(opCode);
        buffer.putShort(errorCode);
        buffer.put(errorMsg.getBytes());
        buffer.put(ZEROBYTE);

        HEADER = buffer.array();
    }

    public void fromBytes(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, bytes.length);
        opCode = buffer.getShort();
        errorCode = buffer.getShort();

        StringBuilder sb = new StringBuilder();
        byte b;
        while ((b = buffer.get()) != ZEROBYTE) {
            sb.append((char) b);
        }
        errorMsg = sb.toString();
    }


}
