package packets;

import codes.OPCODES;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class DataPacket extends Packet {

    short opCode = OPCODES.DATA;
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

        buffer.putShort(opCode);
        buffer.putShort(blockNum);
        buffer.put(data);

        HEADER = buffer.array();

        //maybe logic to get rid of packets and trow error if they exceed 512B
        System.out.println(Arrays.toString(HEADER));
    }

    public void fromBytes(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, bytes.length);
        opCode = buffer.getShort();
        blockNum = buffer.getShort();

        data = new byte[buffer.remaining()];
        buffer.get(data);
    }
}
