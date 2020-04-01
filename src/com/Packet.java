package com;

public class Packet {

    public Byte[] toObjByteArr(byte[] b){
        Byte[] retArr = new Byte[b.length];
        for(int i = 0; i < b.length; i++){
            retArr[i] = b[i];
        }
        return retArr;
    }

    public byte[] toPrimByteArr(Object[] b){
        byte[] retArr = new byte[b.length];
        for(int i = 0; i < b.length; i++){
            retArr[i] = (byte) b[i];
        }
        return retArr;
    }
}
