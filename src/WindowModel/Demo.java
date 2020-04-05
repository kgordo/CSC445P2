public class Demo {
    public static void main(String args[]) throws InterruptedException {

        RunThread rt = new RunThread();
        Thread t = new Thread(rt);
        t.start();
        //starts random numbers

        SemExecutor se = new SemExecutor();
        //starts sliding window
    }
}
