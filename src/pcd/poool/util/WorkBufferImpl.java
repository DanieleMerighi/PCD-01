package pcd.poool.util;

public class WorkBufferImpl extends BoundedBufferImpl<Runnable> implements WorkBuffer {

	private int pendingCounter;

	public WorkBufferImpl(int size) {
		super(size);
	}

	@Override
	public synchronized void put(Runnable runnable) {
		pendingCounter++;
		super.put(runnable);
	}

	@Override
	public synchronized void done() {
		pendingCounter--;
		notifyAll();
	}

	@Override
	public synchronized void waitAll() {
		while (pendingCounter > 0) {
			try {
				wait();
			} catch (InterruptedException ignored) {}
		}
	}

	@Override
	public synchronized void clearAll() {
		if (pendingCounter != 0) {
			pendingCounter = 0;
			notifyAll();
		}
	}
}
