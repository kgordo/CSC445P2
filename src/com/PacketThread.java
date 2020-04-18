package com;

import packets.ACKPacket;
import packets.DataPacket;
import packets.Packet;
import utils.Timeout;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class PacketThread extends Thread {
    Semaphore sem;
    short blockNum;
    int blockIndex;
    InetAddress destination;
    DatagramSocket socket;
    int port;
    byte[] data;
    ACKPacket ack;
    DatagramPacket dataPacket;
    double timeout = 2000;
    double start;
    private volatile boolean exit = false;
    ReentrantLock lock;

    public PacketThread(ReentrantLock lock, Semaphore sem, DatagramPacket dp, short blockNum, InetAddress destination, DatagramSocket socket, int port) {
        this.lock = lock;
        this.sem = sem;
        this.blockNum = blockNum;
        this.blockIndex = blockNum;
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort(blockNum);
        this.data = buffer.array();
        this.port = port;
        this.socket = socket;

        try {
            //this.socket = new DatagramSocket(0, destination);
            this.socket.setSoTimeout((int)timeout);
        } catch (IOException e) {
            System.err.println("Problem creating socket with anonymous port");
        }

        this.destination = destination;
        dataPacket = dp;
    }

    @Override
    public void run() {
        //the ack that the threads are looking for is their own thread number
        System.out.println("Thread" + blockNum + " Is looking for ACK: " + blockIndex);
        try {
            try {
                //once thread is run it gets a permit
                sem.acquire();
                //received used to break thread out of a loop
                //DatagramPacket dp = new DatagramPacket(data, data.length, destination, port);
                try {
                    lock.lock();
                    socket.send(dataPacket);
                    lock.unlock();
                    start = System.currentTimeMillis();
                    Thread.sleep(100);
                    //send packet
                } catch (IOException e) {
                    System.err.println("Problem sending data");
                    e.printStackTrace();
                }
                boolean notReceived = true;
                while (notReceived) {
                    DatagramPacket ackReceived = new DatagramPacket(new byte[ACKPacket.ACKSIZE], ACKPacket.ACKSIZE);
                    lock.lock();
                    socket.receive(ackReceived);
                    lock.unlock();
                    if (ackReceived != null) {
                        double end = System.currentTimeMillis();
                        double rtt = Math.abs(end - start);
                        timeout = Timeout.calculate(rtt);

                        try {
                            socket.setSoTimeout((int)timeout);
                        } catch (SocketException e) {
                           e.printStackTrace();
                        }

                        ack = new ACKPacket(ackReceived.getData());
                        if (!Shared.acks.contains(blockNum)) {
                            Shared.acks.add(ack.getBlockNum(), ack.getBlockNum());
                        }
                        notReceived = false;
                        System.out.println("Received ACK for " + ack.getBlockNum());
                    } else {
                        try {
                            socket.send(dataPacket);
                            //retries if it hasn't been received
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            } catch (InterruptedException exc) {
                System.out.println(exc);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            try {
                socket.setSoTimeout((int)timeout);
            } catch (SocketException e) {
                start = System.currentTimeMillis();
            }

            sem.release();
            System.out.println("Thread " + blockNum + " releases a permit.");
            interrupt();
        }
    }
}
