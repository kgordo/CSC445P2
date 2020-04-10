package com;

import packets.ACKPacket;
import packets.DataPacket;
import packets.ErrorPacket;
import packets.RWPacket;
import utils.XOR;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import static codes.ERRORCODES.*;
import static codes.OPCODES.*;

public class Client {

    //String address = "cs.oswego.edu";
    String address = "localhost";
    ArrayList<byte[]> downloadData = new ArrayList<>();
    InetAddress destination;
    //int port = 2850;
    int port = 1026;
    DatagramSocket socket;

    public void start() throws IOException {

        socket = new DatagramSocket();
        handshake();
        while(true){
            DatagramPacket packet = new DatagramPacket(new byte[512], 512);
            socket.receive(packet);
            handle(packet);
        }

    }

    public void handshake() {
        XOR xor = new XOR();
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            System.err.println("Problem initializing socket");
            e.printStackTrace();
        }
        try {
            destination = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            System.err.println("Problem initializing host");
            e.printStackTrace();
        }
        DatagramPacket clientKey = new DatagramPacket(xor.getKey(), xor.getKey().length, destination, port);
        try {
            socket.send(clientKey);
        } catch (IOException e) {
            System.err.println("Problem sending handshake to server");
            e.printStackTrace();
        }

        byte[] temp = new byte[4];
        DatagramPacket exchange = new DatagramPacket(temp, temp.length);
        try {
            socket.receive(exchange);
        } catch (IOException e) {
            System.err.println("Problem receiving hanshake from server");
            e.printStackTrace();
        }
        xor.finishClientXOR(temp);

        System.out.println(Arrays.toString(xor.getKey()));
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
            DatagramPacket errorPacket = badOpcode.getDataGramPacket(destination, port);
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
            DatagramPacket errorPacket = fileNotFound.getDataGramPacket(destination, port);
            try {
                socket.send(errorPacket);
            } catch (IOException e) {
                System.err.println("Problem sending error message");
                e.printStackTrace();
            }
            return;
        }
        //TODO: Start sending file to server
    }

    public void handleWRQ(byte[] bytes){
        RWPacket wrq = new RWPacket(bytes);
        File fileToUpload = new File(wrq.getFileName());
        if(fileToUpload.exists()){
            ErrorPacket fileAlreadyExists = new ErrorPacket(FILEEXISTS);
            DatagramPacket errorPacket = fileAlreadyExists.getDataGramPacket(destination, port);
            try {
                socket.send(errorPacket);
            } catch (IOException e) {
                System.err.println("Problem sending error message");
                e.printStackTrace();
            }
            return;
        }
        //TODO: Server wants to write file to client
    }

    public void handleDATA(byte[] bytes){
        DataPacket data = new DataPacket(bytes);
        short blockNum = data.getBlockNum();
        byte[] packetData = data.getData();
        //TODO: Do we want to lock here? Should we maybe initialize the instance variable downloadData to some size?
        if(downloadData.get(blockNum) == null) {
            downloadData.add(blockNum, packetData);
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
        Client client = new Client();
        client.start();
    }


}
