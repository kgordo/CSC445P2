package NewSlidingWindowModel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Server {

    public void start() throws IOException {
        DatagramSocket socket = new DatagramSocket(2850);
        while(true) {
            byte[] getIt = new byte[2];
	    //enough for a short
            DatagramPacket packet = new DatagramPacket(getIt, getIt.length);
	    //receve packet
            socket.receive(packet);
            getIt = packet.getData();
            DatagramPacket sendData = new DatagramPacket(getIt, getIt.length, packet.getAddress(), packet.getPort());
	    //send packet
            socket.send(sendData);
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();
    }
}
