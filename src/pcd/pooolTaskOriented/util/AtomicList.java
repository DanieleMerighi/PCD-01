package pcd.pooolTaskOriented.util;

import java.util.List;

public interface AtomicList<T> {

    List<T> getAll();

    void addAll(List<T> list);

    T get(int index);

    void removeElement(T element);

    int size();

    boolean isEmpty();

}
