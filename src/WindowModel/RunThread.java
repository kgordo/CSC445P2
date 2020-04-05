import java.util.Random;

public class RunThread implements Runnable {

    @Override
    public void run() {
        Random r = new Random();
        while(Shared.work){
            Shared.check = r.nextInt(Shared.numThreads);
            System.out.println(Shared.check);
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    //continuously makes random numbers
}
