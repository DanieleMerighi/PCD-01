package pcd.poool.util;

public interface WorkBuffer extends BoundedBuffer<Runnable> {

	void done();

	void waitAll();

}
