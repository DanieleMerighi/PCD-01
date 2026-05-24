package pcd.pooolThreadOrientedJpf.util;

import java.util.Optional;

public interface SynchBox<T> {

    void put(T e);

    Optional<T> get(); //empty if the channel is closed

    void end();
}
