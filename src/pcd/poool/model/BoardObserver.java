package pcd.poool.model;

public interface BoardObserver {
    void modelUpdated(Board board, int framePerSec);
}
