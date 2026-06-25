package pcd.pooolTaskOriented.util;

import java.util.function.Function;

public interface AtomicReference<T> {

    T get();

    void set(T value);

    T getAndSet(T value);

    void map(Function<? super T, ? extends T> mapper);

}
