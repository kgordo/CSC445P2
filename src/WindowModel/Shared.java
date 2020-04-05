import java.util.ArrayList;

class Shared {
    static int check = 0;
    //the number that acks a thread
    static boolean work = true;
    //turns off the random numbers
    static int numThreads = 10;
    //num threads
    static ArrayList<Integer> acks = new ArrayList<>();
    //stores all received acks in order

    public static void setAcks(){
        for(int i = 0; i < numThreads; i++){
            acks.add(-1);
        }
    }
    //-1 used as dummy value

    public static int getLeft(){
        for(int i = 0; i < acks.size(); i++){
            if(acks.get(i) == -1){
                return i;
            }
        }
        return acks.size();
    }
    //finds the furthest right acked packet //|0|1|-1|3|4| -> 1
}
