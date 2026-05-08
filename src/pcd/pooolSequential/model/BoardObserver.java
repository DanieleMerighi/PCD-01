package pcd.pooolSequential.model;

import pcd.pooolSequential.view.BoardViewInfo;

public interface BoardObserver {
    void modelUpdated(BoardViewInfo boardViewInfo, long framePerSec);
    void gameOver(String result);
}
