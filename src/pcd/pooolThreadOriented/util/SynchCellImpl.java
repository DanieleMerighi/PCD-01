package pcd.pooolThreadOriented.util;

import java.util.Optional;

public class SynchCellImpl<T> implements SynchCell<T> {

	private T element = null;
	private boolean closed = false;

	@Override
	public synchronized void put(T e) {
		if (closed) {
			throw new IllegalStateException("put on a closed SynchCell");
		}
		while (element != null) {
			try {
				wait();
			} catch (InterruptedException ignored) {}
		}
		element = e;
		notifyAll();
	}

	@Override
	public synchronized Optional<T> get() {
		while (element == null && !closed) {
			try {
				wait();
			} catch (InterruptedException ignored) {}
		}
		if (element != null) {
			var e = element;
			element = null;
			notifyAll();
			return Optional.of(e);
		}
		return Optional.empty();
	}

	@Override
	public synchronized void end() {
		closed = true;
		notifyAll();
	}

}
