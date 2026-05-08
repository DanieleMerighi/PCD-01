package pcd.pooolThreadOriented.util;

public interface BoundedBuffer<Item> {

    void put(Item item);
    
    Item get();
    
}
