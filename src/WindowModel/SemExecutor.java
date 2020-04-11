import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class SemExecutor {

    Semaphore sem;
    int windowSize = 3;
    Queue<RandNumThread> threads = new LinkedList<>();

    public SemExecutor(){
        Shared.setAcks();
        sem = new Semaphore(windowSize);

        for(int i = 0; i < Shared.numThreads; i++) {
            threads.add(new RandNumThread(sem, i));
        }
        //makes threads
        run();
    }

    public void run(){
        int right = 0;
        //this is easy to track because we only need to account for the
        //last thread run
        int queueSize = Shared.numThreads;
        //queue used to organize execute order
        while(right < queueSize){
            //functions until all threads have been started
            if((right-Shared.getLeft() < windowSize) && sem.availablePermits() > 0){
                //checks to see if there are permits available and that the
                //window size is not broken from left-most ack
                threads.remove().start();
                right++;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while(Shared.getLeft() != right){
            //waits for all acks to be received
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //stops random numbers by setting flag
        Shared.work = false;
    }

}
