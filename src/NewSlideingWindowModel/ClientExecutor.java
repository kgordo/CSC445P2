package NewSlideingWindowModel;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class SemExecutor {

    public class ClientExecutor {

        Semaphore sem;
        int windowSize = 5;
        int port = 2850;
        String address = "localhost";
        Queue<PacketThread> threads = new LinkedList<>();

        public ClientExecutor() throws SocketException, UnknownHostException {
            Shared.setAcks();
            sem = new Semaphore(windowSize);
            InetAddress destination = InetAddress.getByName(address);
            DatagramSocket socket = new DatagramSocket();

            for (short i = 0; i < Shared.numThreads; i++) {
                threads.add(new PacketThread(sem, i, destination, socket, port));
            }
            //makes threads
            PacketCatcher pc = new PacketCatcher(socket);
            Thread packetCatcher = new Thread(pc);
            packetCatcher.start();
            run();
        }

        public void run() {
            int right = 0;
            //this is easy to track because we only need to account for the
            //last thread run
            int queueSize = Shared.numThreads;
            //queue used to organize execute order
            while (right < queueSize) {
                //functions until all threads have been started
                if ((right - Shared.getLeft() < windowSize) && sem.availablePermits() > 0) {
                    //checks to see if there are permits available and that the
                    //window size is not broken from left-most ack
                    threads.remove().start();
                    right++;
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (Shared.getLeft() != right) {
                //waits for all acks to be received
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //stops random numbers by setting flag
            Shared.work = false;
        }

    }
}
