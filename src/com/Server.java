package com;

import packets.ACKPacket;
import packets.DataPacket;
import packets.ErrorPacket;
import packets.RWPacket;
import utils.XOR;
import codes.OPCODES.*;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import static codes.ERRORCODES.*;
import static codes.OPCODES.*;

public class Server {

    ArrayList<byte[]> uploadedData = new ArrayList<>();

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
            handleRRQ(bytes);
        }
        else if(opCode == WRQ){
            handleWRQ(bytes);
        }
        else if(opCode == DATA){
            handleDATA(bytes);
        }
        else if(opCode == ACK){
            handleACK(bytes);
        }
        else if(opCode == ERROR){
            handleError(bytes);
        }
        else{
            ErrorPacket badOpcode = new ErrorPacket(UNDEFINED);
            //TODO:build DataGramPacket and send error to client
        }
    }

    public void handleRRQ(byte[] bytes){
        RWPacket rrq = new RWPacket(bytes);
        File fileToDownload = new File(rrq.getFileName());
        if(!fileToDownload.exists()){
            ErrorPacket fileNotFound = new ErrorPacket(FILENOTFOUND);
            //TODO: Send error to client
            return;
        }
        //TODO: Start sending file to client
    }

    public void handleWRQ(byte[] bytes){
        RWPacket wrq = new RWPacket(bytes);
        File fileToUpload = new File(wrq.getFileName());
        if(fileToUpload.exists()){
            ErrorPacket fileAlreadyExists = new ErrorPacket(FILEEXISTS);
            //TODO: Send error to client
            return;
        }
        //TODO: Client wants to write file to server
    }

    public void handleDATA(byte[] bytes){
        DataPacket data = new DataPacket(bytes);
        short blockNum = data.getBlockNum();
        byte[] packetData = data.getData();
        //TODO: Do we want to lock here? Should we maybe initialize the instance variable uploadedData to some size?
        if(uploadedData.get(blockNum) == null) {
            uploadedData.add(blockNum, packetData);
        }
    }

    public void handleACK(byte[] bytes){
        ACKPacket ack = new ACKPacket(bytes);
        System.out.println("Received ACK for block: " + ack.getBlockNum());
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
