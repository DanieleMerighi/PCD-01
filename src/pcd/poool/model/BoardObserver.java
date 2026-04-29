package pcd.poool.model;

import pcd.poool.view.BoardViewInfo;

import java.util.List;

public interface BoardObserver {
    void modelUpdated(BoardViewInfo boardViewInfo, int framePerSec);
}
