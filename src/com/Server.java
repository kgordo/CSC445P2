package com;

import packets.ACKPacket;
import packets.DataPacket;
import packets.ErrorPacket;
import packets.RWPacket;
import utils.XOR;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import static codes.ERRORCODES.*;
import static codes.OPCODES.*;

public class Server {

    ArrayList<byte[]> uploadedData = new ArrayList<>();
    DatagramSocket socket;
    InetAddress clientAddress;
    //int port = 2850;
    int port = 1026;
    int clientPort;


    public void start() throws IOException {
        socket = new DatagramSocket(port);
        handshake();
        while(true) {

            DatagramPacket packet = new DatagramPacket(new byte[512], 512);
            socket.receive(packet);
            handle(packet);

        }
    }

    public void handshake(){
        byte[] clientKey = new byte[4];
        DatagramPacket packet = new DatagramPacket(clientKey, clientKey.length);
        try {
            socket.receive(packet);
        } catch (IOException e) {
            System.err.println("Problem receiving handshake from client");
            e.printStackTrace();
        }

        clientAddress = packet.getAddress();
        clientPort = packet.getPort();

        clientKey = packet.getData();
        XOR xor = new XOR(clientKey);

        byte[] serverKey = xor.finishServerXOR();
        DatagramPacket sendData = new DatagramPacket(serverKey, serverKey.length, clientAddress, packet.getPort());
        try {
            socket.send(sendData);
        } catch (IOException e) {
            System.err.println("Problem returning handshake to client");
            e.printStackTrace();
        }

        System.out.println(Arrays.toString(xor.key));
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
            DatagramPacket errorPacket = badOpcode.getDataGramPacket(clientAddress, clientPort);
            try {
                socket.send(errorPacket);
            } catch (IOException e) {
                System.err.println("Problem sending error message");
                e.printStackTrace();
            }
        }
    }

    public void handleRRQ(byte[] bytes){
        RWPacket rrq = new RWPacket(bytes);
        File fileToDownload = new File(rrq.getFileName());
        if(!fileToDownload.exists()){
            ErrorPacket fileNotFound = new ErrorPacket(FILENOTFOUND);
            DatagramPacket errorPacket = fileNotFound.getDataGramPacket(clientAddress, clientPort);
            try {
                socket.send(errorPacket);
            } catch (IOException e) {
                System.err.println("Problem sending error message");
                e.printStackTrace();
            }
            return;
        }
        //TODO: Start sending file to client
    }

    public void handleWRQ(byte[] bytes){
        RWPacket wrq = new RWPacket(bytes);
        File fileToUpload = new File(wrq.getFileName());
        if(fileToUpload.exists()){
            ErrorPacket fileAlreadyExists = new ErrorPacket(FILEEXISTS);
            DatagramPacket errorPacket = fileAlreadyExists.getDataGramPacket(clientAddress, clientPort);
            try {
                socket.send(errorPacket);
            } catch (IOException e) {
                System.err.println("Problem sending error message");
                e.printStackTrace();
            }
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
