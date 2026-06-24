package pcd.pooolTaskOriented.util;

import java.util.function.Function;

/**
 * 
 * Simple implementation of an atomic reference
 * as a monitor
 * 
 * @param <T>
 */
public class AtomicReferenceImpl<T> implements AtomicReference<T> {

	private T value;

	public AtomicReferenceImpl(T initialValue) {
		value = initialValue;
	}

	@Override
	public synchronized T get() {
		return value;
	}

	@Override
	public synchronized void set(T value) {
		this.value = value;
	}

	@Override
	public synchronized T getAndSet(T value) {
		var ret = this.value;
		this.value = value;
		return ret;
	}

	@Override
	public synchronized void map(Function<? super T, ? extends T> mapper) {
		this.value = mapper.apply(value);
	}

}
