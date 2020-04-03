package com.company;

import java.nio.ByteBuffer;
import java.util.Random;

public class XOR {
    byte[] key;
    Random r = new Random();

    public XOR(){
        key = new byte[4];
        r.nextBytes(key);
    }

    public XOR(byte[] received){
        key = received;
    }

    public byte[] finishServerXOR(){
        ByteBuffer temp = ByteBuffer.allocate(8);

        temp.put(key);

        byte[] endConCat = new byte[4];
        r.nextBytes(endConCat);
        temp.put(endConCat);

        key = temp.array();
        return endConCat;
    }

    public void finishClientXOR(byte[] received){
        ByteBuffer temp = ByteBuffer.allocate(8);
        temp.put(key);
        temp.put(received);
        key = temp.array();
    }

    private byte decode(byte k, byte n){
        return (byte) (n ^ k);
    }
    //uses bitshift xor to encrypt one byte

    public byte[] fullDecode(byte[] data){
        //give this method a byte[] and it will encrypt every cumulative
        //64 bits with the same xor keys
        byte[] result = new byte[data.length];
        int mod = 0;
        for(int i = 0; i < data.length; i++){
            result[i] = decode(key[mod], data[i]);
            //uses xor method to do calculation
            mod++;
            if(!(mod < key.length)){
                mod = 0;
            }
            //logic to make this work
        }
        return result;
    }

    public byte[] getKey(){
        return key;
    }
}
