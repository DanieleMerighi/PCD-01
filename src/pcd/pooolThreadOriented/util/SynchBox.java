package pcd.pooolThreadOriented.util;

public interface SynchBox<T> {

	void put(T e);

	T get();

}
