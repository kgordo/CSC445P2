package utils;

import packets.DataPacket;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

public interface Data {

    int MAX_SIZE = 512;

    static ArrayList<DatagramPacket> buildDataPackets(String fileName, InetAddress host, int port) throws IOException {


        ArrayList<DatagramPacket> dataPackets = new ArrayList<>();

        BufferedInputStream fileStream = new BufferedInputStream(new FileInputStream(fileName));
        int bytesRead = 0;
        short blockNum = 0;
        byte[] dataBuffer = new byte[512];

        // Read 512 bytes at a time until end of file reached
        while((bytesRead = fileStream.read(dataBuffer, 0, MAX_SIZE)) != -1){

        // If less than 512 bytes have been read, adjust the size of the byte[] accordingly
            if(bytesRead < MAX_SIZE){
               dataBuffer = Arrays.copyOfRange(dataBuffer, 0, bytesRead);
            }

        // Build DataPacket and add to ArrayList of packets
            DataPacket dataPacket = new DataPacket(blockNum, dataBuffer);
            dataPacket.buildPacket(host, port);
            dataPackets.add(dataPacket.getDataGramPacket());

        // Increment block number
        blockNum++;

        // This is probably redundant
            dataBuffer= new byte[512];
        }
        return dataPackets;
    }

}
