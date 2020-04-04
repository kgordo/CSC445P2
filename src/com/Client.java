package com;

import utils.XOR;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class Client {

    String address = "cs.oswego.edu";

    public void start() throws IOException {

        XOR xor = new XOR();
        DatagramSocket socket = new DatagramSocket();
        InetAddress destination = InetAddress.getByName(address);
        DatagramPacket clientKey = new DatagramPacket(xor.getKey(), xor.getKey().length, destination, 2850);
        socket.send(clientKey);

        byte[] temp = new byte[4];
        DatagramPacket exchange = new DatagramPacket(temp, temp.length);
        socket.receive(exchange);
        xor.finishClientXOR(temp);

        System.out.println(Arrays.toString(xor.getKey()));
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        client.start();
    }


}
