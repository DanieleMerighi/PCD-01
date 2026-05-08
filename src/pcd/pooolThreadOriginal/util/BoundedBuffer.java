package pcd.pooolThreadOriginal.util;

public interface BoundedBuffer<Item> {

    void put(Item item);
    
    Item get();
    
}
