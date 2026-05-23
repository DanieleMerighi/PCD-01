package pcd.pooolThreadOrientedJpf.util;

public interface SynchBox<T> {

    void put(T e);

    T get();
}
