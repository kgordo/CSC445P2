package utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public interface Data {

    int MAX_SIZE = 512;

    static ArrayList<byte[]> buildData(String fileName) throws IOException {

        ArrayList<byte []> dataChunks = new ArrayList<>();
        BufferedInputStream fileStream = new BufferedInputStream(new FileInputStream(fileName));
        int bytesRead = 0;
        byte[] dataBuffer = new byte[512];

        // Read 512 bytes at a time until end of file reached
        while((bytesRead = fileStream.read(dataBuffer, 0, MAX_SIZE)) != -1){

        // If less than 512 bytes have been read, adjust the size of the byte[] accordingly
            if(bytesRead < MAX_SIZE){
                dataBuffer = Arrays.copyOfRange(new byte[bytesRead], 0, bytesRead);
            }

        // Add byte[] to ArrayList of payloads
            dataChunks.add(dataBuffer);

        // This is probably redundant
            dataBuffer= new byte[512];
        }
        return dataChunks;
    }

}
