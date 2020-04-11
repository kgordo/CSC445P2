package NewSlidingWindowModel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class PacketCatcher implements Runnable {
    DatagramSocket socket;

    public PacketCatcher(DatagramSocket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        while(Shared.work){
	    //thread stops when system stops
            byte[] rec = new byte[2];
            DatagramPacket recPak = new DatagramPacket(rec, rec.length);
            try {
                socket.receive(recPak);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteBuffer buffer = ByteBuffer.wrap(recPak.getData());
            short add = buffer.getShort();
            if(!Shared.acks.contains(add)) {
		//avoid dupes for any ack
                Shared.acks.set(add, add);
		//sets the shared int array to confirm that the thread has received an ack,
        	//and hopefully push the left value forward
                System.out.println("Received: " + add);
                System.out.println(Arrays.toString(Shared.acks.toArray()) + "   " + Shared.acks.size());
            }
        }
    }
}
