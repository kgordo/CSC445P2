package packets;

import utils.Data;
import codes.OPCODES.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import static codes.OPCODES.WRQ;

public class RWPacket extends Packet {

    static final String MODE = "octet";
    short opCode;
    String fileName;
    int size;

    public RWPacket(short opCode, String fileName) {
        this.opCode = opCode;
        this.fileName = fileName;
        this.size = 4 + fileName.getBytes().length + MODE.getBytes().length;
        buildHeader();
        encryptDecrypt();
    }

    public RWPacket(byte[] bytes){
       // encryptDecrypt(bytes);
        fromBytes(bytes);
    }

    private void buildHeader(){

        ByteBuffer buffer = ByteBuffer.allocate(size);

        buffer.putShort(opCode);
        buffer.put(fileName.getBytes());
        buffer.put(ZEROBYTE);
        buffer.put(MODE.getBytes());
        buffer.put(ZEROBYTE);

        HEADER = buffer.array();
    }

    public void fromBytes(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, bytes.length);
        opCode = buffer.getShort();

        StringBuilder sb = new StringBuilder();
        byte b;
        while ((b = buffer.get()) != ZEROBYTE) {
            sb.append((char) b);
        }
        fileName = sb.toString();

        size = 4 + MODE.getBytes().length + fileName.getBytes().length;
        buildHeader();
    }

    public String getFileName(){return fileName;}
}
