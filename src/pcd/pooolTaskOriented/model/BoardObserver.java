package pcd.pooolTaskOriented.model;

import pcd.pooolTaskOriented.view.BoardViewInfo;
import pcd.pooolTaskOriented.view.GameStateViewInfo;

public interface BoardObserver {

    void modelUpdated(BoardViewInfo boardViewInfo, GameStateViewInfo gameStateViewInfo, long tickPerSec);

    void gameOver(BoardViewInfo boardViewInfo, GameStateViewInfo gameStateViewInfo, long tickPerSec, String result);

}
