package com;

import codes.OPCODES;
import packets.*;
import utils.Data;
import utils.XOR;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

import static codes.ERRORCODES.*;
import static codes.OPCODES.*;

public class Server {
    Semaphore sem;
    ArrayList<byte[]> uploadData = new ArrayList<>(100);
    DatagramSocket socket;
    DatagramSocket threadSocket;
    InetAddress clientAddress;
    int port;
    int clientPort;
    static int WINDOWSIZE = 7;
    static final int MAXDATASIZE = 512;
    static final int MAXPACKETSIZE = 516;
    boolean dropPackets;


    public void start() throws IOException {
        Scanner stdin = new Scanner(System.in);

        System.out.println("Enter port:");
        port = Integer.parseInt(stdin.nextLine());

        //Set address preference
        System.out.println("IPv6? (Y/N):");
        String format = stdin.nextLine();

        if (format.equalsIgnoreCase("y")) {
            System.setProperty("java.net.preferIPv4Stack", "false");
        } else if (format.equalsIgnoreCase("n")) {
            System.setProperty("java.net.preferIPv4Stack", "true");
        }

        //Set drop preference
        System.out.println("Drop 1% of packets (Y/N):");
        String drop = stdin.nextLine();

        if (drop.equalsIgnoreCase("y")) {
            dropPackets = true;
        } else if (drop.equalsIgnoreCase("n")) {
            dropPackets = false;
        }

        //Get window size
        System.out.println("Please enter desired window size");
        WINDOWSIZE = Integer.parseInt(stdin.nextLine());

        setUploadData();
        sem = new Semaphore(WINDOWSIZE);
        socket = new DatagramSocket(port);
        handshake();
        while(true) {
            DatagramPacket packet = new DatagramPacket(new byte[MAXPACKETSIZE], MAXPACKETSIZE);
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
        Packet.encryptDecrypt(bytes);
        short opCode = ByteBuffer.wrap(bytes).getShort();

        if(opCode == RRQ){
            handleRRQ(packet);
        }
        else if(opCode == WRQ){
            handleWRQ(packet);
        }
        else if(opCode == DATA){
            try {
                handleDATA(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(opCode == ACK){
            handleACK(packet);
        }
        else if(opCode == ERROR){
            handleError(packet);
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

    public void handleRRQ(DatagramPacket packet){
        System.out.println("Receives RRQ from Client");
        Shared.setWork(true);
        byte[] bytes = packet.getData();
        RWPacket rrq = new RWPacket(bytes);
        String fileName = rrq.getFileName();
        File fileToDownload = new File(fileName);
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
        //Start sending file to client
        sendData(fileName);
    }

    public void sendData(String file){
        Shared.setWork(true);
        ArrayList<DatagramPacket> dataPackets = new ArrayList<>();
        Queue<PacketThread> threads = new LinkedList<>();
        try {
            //threadSocket = new DatagramSocket(0, clientAddress);
            threadSocket = new DatagramSocket(0);
        } catch (SocketException e) {
            System.err.println("Problem instantiating packetThread socket");
            e.printStackTrace();
        }
        try {
            dataPackets = Data.buildDataPackets(file, clientAddress, clientPort);
        } catch (IOException e) {
            System.err.println("Problem building data");
            e.printStackTrace();
        }
        for(int i = 0; i < dataPackets.size(); ++i){
            short blockNum = (short) i;
            PacketThread packetThread = new PacketThread(sem, dataPackets.get(i), blockNum, clientAddress, threadSocket, clientPort, dropPackets);
            threads.add(packetThread);
        }
        Shared.setAcks(threads.size());
        int right = 0;
        //this is easy to track because we only need to account for the
        //last thread run
        int queueSize = threads.size();
        //queue used to organize execute order
        while (right < queueSize && !threads.isEmpty()) {
            //functions until all threads have been started
            if ((right - Shared.getLeft() < WINDOWSIZE) && sem.availablePermits() > 0) {
                //checks to see if there are permits available and that the
                //window size is not broken from left-most ack
                threads.remove().start();
                right++;
                //Start window over once we've reached it
                if(right == WINDOWSIZE){
                    right = 0;
                }
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        sem.release(WINDOWSIZE);
        System.out.println("Average Throughput in ms: "+ ((WINDOWSIZE * MAXPACKETSIZE)/Shared.getAverage(dataPackets.size())));
    }

    public void handleWRQ(DatagramPacket packet){
        byte[] bytes = packet.getData();
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
        RWPacket rrq = new RWPacket(RRQ, wrq.getFileName());
        try {
            socket.send(rrq.getDataGramPacket(packet.getAddress(), packet.getPort()));
        } catch (IOException e) {
            System.err.println("Problem sending RRQ to client");
            e.printStackTrace();
        }
    }

    public void handleDATA(DatagramPacket packet) throws IOException {
        int dataLength = packet.getLength();
        byte[] bytes = Arrays.copyOfRange(packet.getData(), 0, dataLength);
        Packet.encryptDecrypt(bytes);
        DataPacket data = new DataPacket(bytes);
        short blockNum = data.getBlockNum();
        byte[] packetData = data.getData();

        //If the byte[] at the given position in the downloaded data has length 0, add the block of data to downloadData
        if (uploadData.get(blockNum).length == 0) {
            uploadData.add(blockNum, packetData);
            ACKPacket ack = new ACKPacket(blockNum);
            DatagramPacket ackPacket = ack.getDataGramPacket(packet.getAddress(), packet.getPort());
            try {
                socket.send(ackPacket);
            } catch (IOException e) {
                System.err.println("Problem sending ACK to client");
                e.printStackTrace();
            }
        }
        if (packetData.length < MAXDATASIZE) {
            File download = new File(System.getProperty("user.home") + "/Desktop" + "/download");
            download.createNewFile();
            FileOutputStream fos = new FileOutputStream(download, true);
            for (byte[] block : uploadData) {
                fos.write(block);
            }
            fos.close();
            Shared.setWork(false);
            System.out.println("File successfully uploaded.");
        }
    }

    public void handleACK(DatagramPacket packet){
        byte[] bytes = packet.getData();
        ACKPacket ack = new ACKPacket(bytes);
        System.out.println("Received ACK for block: " + ack.getBlockNum());
    }

    public void handleError(DatagramPacket packet){
        byte[] bytes = packet.getData();
        ErrorPacket errorPacket = new ErrorPacket(bytes);
        System.out.println(errorPacket.getErrorMessage());
        Shared.setWork(false);
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();
    }

    void setUploadData(){
        for(int i = 0; i < 100; ++i){
            uploadData.add(i, new byte[0]);
        }
    }
}
