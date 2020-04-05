import java.util.concurrent.Semaphore;

class RandNumThread extends Thread {
    Semaphore sem;
    int threadName;

    public RandNumThread(Semaphore sem, int threadName) {
        this.sem = sem;
        this.threadName = threadName;
    }

    @Override
    public void run() {
            int relNum = threadName;
            //the ack that the threads are looking for is their own thread number
            System.out.println("Start t" + threadName + " Is looking for: " + relNum);
            try {
                //once thread is run it gets a permit
                sem.acquire();
                //b used to break thread out of a loop
                boolean b = true;
                while(b){
                    if(relNum == Shared.check){
                        //checks shared int to see if the thread has "received an ack"
                        b = false;
                    }
                    Thread.sleep(100);
                }

            } catch (InterruptedException exc) {
                System.out.println(exc);
            }

            System.out.println("t" + threadName + " releases a permit.");
            Shared.acks.set(threadName, threadName);
            //sets the shared int array to confirm that the thread has received an ack,
            //and hopefully push the left value forward
            sem.release();
        }

}
