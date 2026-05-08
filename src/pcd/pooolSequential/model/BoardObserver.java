package pcd.pooolSequential.model;

import pcd.pooolSequential.view.BoardViewInfo;
import pcd.pooolSequential.view.GameStateViewInfo;

public interface BoardObserver {

    void modelUpdated(BoardViewInfo boardViewInfo, GameStateViewInfo gameStateViewInfo, long tickPerSec);

    void gameOver(String result);

}
