package pcd.poool.model;

import pcd.poool.view.BoardViewInfo;
import pcd.poool.view.GameStateViewInfo;

import java.util.List;

public interface BoardObserver {
    void modelUpdated(BoardViewInfo boardViewInfo, GameStateViewInfo gameStateViewInfo, long framePerSec);
    void gameOver(String result);
}
