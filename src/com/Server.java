package com;

import packets.ACKPacket;
import packets.DataPacket;
import packets.ErrorPacket;
import packets.RWPacket;
import utils.XOR;
import codes.OPCODES.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static codes.ERRORCODES.UNDEFINED;
import static codes.OPCODES.*;

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

    public void handle(DatagramPacket packet){
        byte[] bytes = packet.getData();
        short opCode = ByteBuffer.wrap(bytes).getShort();

        if(opCode == RRQ){

        }
        else if(opCode == WRQ){

        }
        else if(opCode == DATA){

        }
        else if(opCode == ACK){

        }
        else if(opCode == ERROR){

        }
        else{
            ErrorPacket badOpcode = new ErrorPacket(UNDEFINED);
            //build DataGramPacket and send error back to client
        }
    }

    public void handleRRQ(byte[] bytes){

    }

    public void handleWRQ(byte[] bytes){

    }

    public void handleDATA(byte[] bytes){

    }

    public void handleACK(byte[] bytes){

    }

    public void handleError(byte[] bytes){
        ErrorPacket errorPacket = new ErrorPacket(bytes);
        System.out.println(errorPacket.getErrorMessage());
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();
    }
}
