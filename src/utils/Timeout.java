package utils;

import java.net.InetAddress;

public class Timeout {

    //Values & algorithm from http://www.scs.stanford.edu/08sp-cs144/notes/l6.pdf
    private static final double ALPHA = 0.125;
    private static final double BETA = 0.25;
    private static double estRTT = 1000;
    private static double devRTT = 100;
    private static double timeout = 0;

    public static final int MAXTIMEOUT = Integer.MAX_VALUE;

    public static double calculate(double sampleRTT){
        estRTT = (1-ALPHA) * estRTT + (ALPHA*sampleRTT);
        devRTT = (1-BETA) * devRTT + Math.abs(sampleRTT - estRTT);
        timeout = estRTT + 4*devRTT;
        System.out.println(timeout);
        return timeout;
    }



}
