package pcd.pooolThreadOriented.model;

import pcd.pooolThreadOriented.view.BoardViewInfo;
import pcd.pooolThreadOriented.view.GameStateViewInfo;

public interface BoardObserver {

    void modelUpdated(BoardViewInfo boardViewInfo, GameStateViewInfo gameStateViewInfo, long tickPerSec);

    void gameOver(BoardViewInfo boardViewInfo, GameStateViewInfo gameStateViewInfo, long tickPerSec, String result);

}
