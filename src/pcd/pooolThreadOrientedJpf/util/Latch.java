package pcd.pooolThreadOrientedJpf.util;

public interface Latch {

    void await();

    void countDown();

    void reset(int initialCount);
}
