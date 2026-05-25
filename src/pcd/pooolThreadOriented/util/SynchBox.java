package pcd.pooolThreadOriented.util;

import java.util.Optional;

public interface SynchBox<T> {

	void put(T e);

	Optional<T> get();

	void end();

}
