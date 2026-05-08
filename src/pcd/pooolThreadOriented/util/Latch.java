package pcd.pooolThreadOriented.util;

public interface Latch {

    void await();

    void countDown();

	void reset(int initialCount);

}
