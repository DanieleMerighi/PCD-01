package pcd.pooolThreadOrientedJpf.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Simple implementation of an atomic list
 * as a monitor
 * 
 * @param <T>
 */
public class AtomicListImpl<T> implements AtomicList<T> {

	private final List<T> value;

	public AtomicListImpl(List<T> initialValue) {
		value = new ArrayList<>(initialValue);
	}

	@Override
	public synchronized List<T> getAll() {
		return List.copyOf(value);
	}

	@Override
	public synchronized void addAll(List<T> list) {
		value.addAll(list);
	}

	@Override
	public T get(int index) {
		return value.get(index);
	}

	@Override
	public synchronized AtomicList<T> subList(int start, int end) {
		return new AtomicListImpl<>(value.subList(start, end));
	}

	@Override
	public synchronized void removeElement(T element) {
		value.remove(element);
	}

	@Override
	public synchronized int size() {
		return value.size();
	}

	@Override
	public synchronized boolean isEmpty() {
		return value.isEmpty();
	}

}
