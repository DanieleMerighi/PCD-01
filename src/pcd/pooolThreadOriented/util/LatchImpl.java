package pcd.pooolThreadOriented.util;

public class LatchImpl implements Latch {

    private final int nCountDowns;
    private int nCounts;

    public LatchImpl(int nCountDowns) {
        this.nCountDowns = nCountDowns;
        nCounts = 0;
    }

    @Override
    public synchronized void await() {
        while (nCounts < nCountDowns) {
            try {
                wait();
            } catch (InterruptedException ignored) {}
        }
    }

    @Override
    public synchronized void countDown() {
        nCounts++;
        if (nCounts == nCountDowns) {
            notifyAll();
        }
    }

}
