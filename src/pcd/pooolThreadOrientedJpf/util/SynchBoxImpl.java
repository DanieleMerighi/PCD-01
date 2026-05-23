package pcd.pooolThreadOrientedJpf.util;

public class SynchBoxImpl<T> implements SynchBox<T> {

    private T element;

    @Override
    public synchronized void put(T e) {
        while (element != null) {
            try {
                wait();
            } catch (InterruptedException ignored) {
            }
        }
        element = e;
        notifyAll();
    }

    @Override
    public synchronized T get() {
        while (element == null) {
            try {
                wait();
            } catch (InterruptedException ignored) {
            }
        }
        T e = element;
        element = null;
        notifyAll();
        return e;
    }
}
