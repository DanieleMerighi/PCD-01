package pcd.pooolTaskOriented.util;

public interface BoundedBuffer<Item> {

    void put(Item item);
    
    Item get();
    
}
