package NewSlidingWindowModel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

class PacketThread extends Thread {
    Semaphore sem;
    short threadName;
    int threadIndex;
    InetAddress destination;
    DatagramSocket socket;
    int port;
    byte[] data;

    public PacketThread(Semaphore sem, short threadName, InetAddress destination, DatagramSocket socket, int port) {
        this.sem = sem;
        this.threadName = threadName;
        this.threadIndex = threadName;
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort(threadName);
        this.data = buffer.array();
        this.port = port;
        this.socket = socket;
        this.destination = destination;

    }

    @Override
    public void run() {
        //the ack that the threads are looking for is their own thread number
        System.out.println("Start t" + threadName + " Is looking for: " + threadIndex);
        try {
            //once thread is run it gets a permit
            sem.acquire();
            //b used to break thread out of a loop
            DatagramPacket dp = new DatagramPacket(data, data.length, destination, port);
            try {
                socket.send(dp);
		//send packet
            } catch (IOException e) {
                e.printStackTrace();
            }
            boolean b = true;
            while (b) {
                if (Shared.acks.contains(threadName)) {
                    //checks shared int to see if the thread has received an ack
                    b = false;
                }
                try {
                    socket.send(dp);
		    //retries if it hasn't been received
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Thread.sleep(10);
            }

        } catch (InterruptedException exc) {
            System.out.println(exc);
        }

        System.out.println("t" + threadName + " releases a permit.");
        sem.release();
    }
}
