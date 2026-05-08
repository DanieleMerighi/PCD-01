package pcd.pooolThreadOriented.util;

public class LatchImpl implements Latch {

    private int counter;

    public LatchImpl(int initialCount) {
        counter = initialCount;
    }

    @Override
    public synchronized void await() {
        while (counter > 0) {
            try {
                wait();
            } catch (InterruptedException ignored) {}
        }
    }

    @Override
    public synchronized void countDown() {
        counter--;
        if (counter == 0) {
            notifyAll();
        }
    }

    @Override
    public synchronized void reset(int initialCount) {
        counter = initialCount;
        notifyAll();
    }

}
