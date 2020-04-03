package com.company;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class Server {

    public void start() throws IOException {
        DatagramSocket socket = new DatagramSocket(2850);
        while(true) {

            byte[] clientKey = new byte[4];
            DatagramPacket packet = new DatagramPacket(clientKey, clientKey.length);
            socket.receive(packet);

            clientKey = packet.getData();
            XOR xor = new XOR(clientKey);

            byte[] serverKey = xor.finishServerXOR();
            DatagramPacket sendData = new DatagramPacket(serverKey, serverKey.length, packet.getAddress(), packet.getPort());
            socket.send(sendData);

            System.out.println(Arrays.toString(xor.key));
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();
    }
}
