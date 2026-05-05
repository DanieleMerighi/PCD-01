package pcd.poool.util;

import java.util.LinkedList;

/**
 * 
 * Simple implementation of a bounded buffer
 * as a monitor, using raw mechanisms
 * 
 * @param <Item>
 */
public class BoundedBufferImpl<Item> implements BoundedBuffer<Item> {

	private final LinkedList<Item> buffer;
	private final int maxSize;

	public BoundedBufferImpl(int size) {
		buffer = new LinkedList<>();
		maxSize = size;
	}

	@Override
	public synchronized void put(Item item) {
		while (isFull()) {
			try {
				wait();
			} catch (InterruptedException ignored) {}
		}
		buffer.addLast(item);
		notifyAll();
	}

	@Override
	public synchronized Item get() {
		while (isEmpty()) {
			try {
				wait();
			} catch (InterruptedException ignored) {}
		}
		Item item = buffer.removeFirst();
		notifyAll();
		return item;
	}

	private boolean isFull() {
		return buffer.size() == maxSize;
	}

	private boolean isEmpty() {
		return buffer.isEmpty();
	}
}
