package com;

import codes.OPCODES;
import packets.*;
import utils.Data;
import utils.XOR;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Semaphore;

import static codes.ERRORCODES.*;
import static codes.OPCODES.*;
import static com.Server.MAXDATASIZE;
import static com.Server.MAXPACKETSIZE;

public class Client {
    static int WINDOWSIZE = 7;
    private static final byte ZEROBYTE = 0;
    Semaphore sem;
    String address;
    ArrayList<byte[]> downloadData = new ArrayList<>(100);
    InetAddress serverAddress;
    int port;
    DatagramSocket socket;
    DatagramSocket threadSocket;
    String fileName;
    boolean dropPackets;

    public void start() throws IOException, InterruptedException {
        setDownloadData();
        sem = new Semaphore(WINDOWSIZE);
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

        //Get server address
        System.out.println("Please enter Server IP: ");
        address = stdin.nextLine();

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

        System.out.println("Upload or Download (U/D):");
        String uploadDownload = stdin.nextLine();
        if (uploadDownload.equalsIgnoreCase("u")) {
            System.out.println("Enter file to upload: ");
        } else if (uploadDownload.equalsIgnoreCase("d")) {
            System.out.println("Enter file to download: ");
        }
        fileName = stdin.nextLine();

        socket = new DatagramSocket();
        handshake();

        if (uploadDownload.equalsIgnoreCase("u")) {
            RWPacket wrq = new RWPacket(OPCODES.WRQ, fileName);
            socket.send(wrq.getDataGramPacket(serverAddress, port));
            while (Shared.getWork()) {
                DatagramPacket packet = new DatagramPacket(new byte[MAXPACKETSIZE], MAXPACKETSIZE);
                socket.receive(packet);
                handle(packet);
            }

        } else if (uploadDownload.equalsIgnoreCase("d")) {
            RWPacket rrq = new RWPacket(OPCODES.RRQ, fileName);
            socket.send(rrq.getDataGramPacket(serverAddress, port));
            while (Shared.getWork()) {
                DatagramPacket packet = new DatagramPacket(new byte[516], 516);
                socket.receive(packet);
                handle(packet);
            }
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
            serverAddress = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            System.err.println("Problem initializing host");
            e.printStackTrace();
        }
        DatagramPacket clientKey = new DatagramPacket(xor.getKey(), xor.getKey().length, serverAddress, port);
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
            System.err.println("Problem receiving handshake from server");
            e.printStackTrace();
        }
        xor.finishClientXOR(temp);

        System.out.println(Arrays.toString(xor.getKey()));
    }

    public void handle(DatagramPacket packet) {
        byte[] bytes = packet.getData();
        Packet.encryptDecrypt(bytes);
        int dataLength = packet.getLength();
        bytes = Arrays.copyOfRange(bytes, 0, dataLength);
        Packet.encryptDecrypt(bytes);
        short opCode = ByteBuffer.wrap(bytes).getShort();
        if (bytes.length > 0) {
            if (opCode == RRQ) {
                handleRRQ(bytes);
            } else if (opCode == WRQ) {
                handleWRQ(bytes);
            } else if (opCode == DATA) {
                try {
                    handleDATA(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (opCode == ACK) {
                handleACK(bytes);
            } else if (opCode == ERROR) {
                handleError(bytes);
            } else {
                ErrorPacket badOpcode = new ErrorPacket(UNDEFINED);
                DatagramPacket errorPacket = badOpcode.getDataGramPacket(serverAddress, port);
                try {
                    socket.send(errorPacket);
                } catch (IOException e) {
                    System.err.println("Problem sending error message");
                    e.printStackTrace();
                }
            }
        }
    }

    public void handleRRQ(byte[] bytes) {
        RWPacket rrq = new RWPacket(bytes);
        String fileName = rrq.getFileName();
        File fileToUpload = new File(fileName);
        if (!fileToUpload.exists()) {
            ErrorPacket fileNotFound = new ErrorPacket(FILENOTFOUND);
            DatagramPacket errorPacket = fileNotFound.getDataGramPacket(serverAddress, port);
            try {
                socket.send(errorPacket);
            } catch (IOException e) {
                System.err.println("Problem sending error message");
                e.printStackTrace();
            }
            return;
        }
        //Send data to server
        sendData(fileName);
    }

    public void sendData(String file) {
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
            dataPackets = Data.buildDataPackets(file, serverAddress, port);
        } catch (IOException e) {
            System.err.println("Problem building data");
            e.printStackTrace();
        }
        for(int i = 0; i < dataPackets.size(); ++i){
            short blockNum = (short) i;
            PacketThread packetThread = new PacketThread(sem, dataPackets.get(i), blockNum, serverAddress, threadSocket, port, dropPackets);
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
        Shared.setWork(false);
        System.out.println("Average Throughput in ms: "+ ((WINDOWSIZE * MAXPACKETSIZE)/Shared.getAverage(dataPackets.size())));
    }


    public void handleWRQ(byte[] bytes) {
        RWPacket wrq = new RWPacket(bytes);
        File fileToDownload = new File(wrq.getFileName());
        if (fileToDownload.exists()) {
            ErrorPacket fileAlreadyExists = new ErrorPacket(FILEEXISTS);
            DatagramPacket errorPacket = fileAlreadyExists.getDataGramPacket(serverAddress, port);
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
            socket.send(rrq.getDataGramPacket(serverAddress, port));
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
        if (downloadData.get(blockNum).length == 0) {
            downloadData.add(blockNum, packetData);
            ACKPacket ack = new ACKPacket(blockNum);
            DatagramPacket ackPacket = ack.getDataGramPacket(packet.getAddress(), packet.getPort());
            try {
                socket.send(ackPacket);
            } catch (IOException e) {
                System.err.println("Problem sending ACK to server");
                e.printStackTrace();
            }
        }
        if (packetData.length < MAXDATASIZE) {
            File download = new File(System.getProperty("user.home") + "/Desktop" + "/download");
            download.createNewFile();
            FileOutputStream fos = new FileOutputStream(download, true);
            for (byte[] block : downloadData) {
                fos.write(block);
            }
            fos.close();
            Shared.setWork(false);
        }
    }

    public void handleACK(byte[] bytes) {
        ACKPacket ack = new ACKPacket(bytes);
        System.out.println("Received ACK for block: " + ack.getBlockNum());
    }

    public void handleError(byte[] bytes) {
        ErrorPacket errorPacket = new ErrorPacket(bytes);
        System.out.println(errorPacket.getErrorMessage());
        Shared.setWork(false);
    }

    void setDownloadData() {
        for (int i = 0; i < 100; ++i) {
            downloadData.add(i, new byte[0]);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = new Client();
        client.start();
    }
}
