package pcd.pooolThreadOriented.util;

import java.util.Optional;

public interface SynchCell<T> {

	void put(T e);

	Optional<T> get();

	void end();

}
