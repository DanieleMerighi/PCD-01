package pcd.pooolThreadOriginal.model;

import pcd.pooolThreadOriginal.view.BoardViewInfo;
import pcd.pooolThreadOriginal.view.GameStateViewInfo;

public interface BoardObserver {

    void modelUpdated(BoardViewInfo boardViewInfo, GameStateViewInfo gameStateViewInfo, long tickPerSec);

    void gameOver(String result);

}
