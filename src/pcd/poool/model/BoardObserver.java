package pcd.poool.model;

import pcd.poool.view.BoardViewInfo;
import pcd.poool.view.GameStateViewInfo;

public interface BoardObserver {

    void modelUpdated(BoardViewInfo boardViewInfo, GameStateViewInfo gameStateViewInfo, long tickPerSec);

    void gameOver(String result);

}
