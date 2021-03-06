package com;

import java.util.ArrayList;

class Shared {
    //the number that acks a thread
    static boolean work = true;
    //turns off the random numbers
    static int numThreads = 15;
    //num threads
    static ArrayList<Short> acks = new ArrayList<>();
    //stores all received acks in order
    static double ttAvg = 0;

    public static void setAcks(int numAcks){
        for(int i = 0; i < numAcks; i++){
            acks.add(Short.MIN_VALUE);
        }
    }
    //-1 used as dummy value

    public static int getLeft(){
        for(int i = 0; i < acks.size(); i++){
            if(acks.get(i) == Short.MIN_VALUE){
                return i;
            }
        }
        return acks.size();
    }

    //finds the furthest right acked packet //|0|1|-1|3|4| -> 1

    public static void setWork(boolean b){work = b;}
    public static boolean getWork(){return work;}

    public static void updateAverage(double tt){
        ttAvg += tt;
    }
    public static double getAverage(int numThreads){
        return ttAvg/numThreads;
    }
}
