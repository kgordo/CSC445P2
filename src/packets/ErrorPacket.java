package packets;

import codes.ERRORCODES;
import codes.OPCODES;
import utils.Data;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static codes.ERRORCODES.FILENOTFOUND;

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
        encryptDecrypt();
    }

    public ErrorPacket(byte[] bytes){
        //encryptDecrypt(bytes);
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

        size = FIXEDHEADERSIZE + errorMsg.length();
        buildHeader();
    }

    public String getErrorMessage() {
        return errorMsg;
    }
}
