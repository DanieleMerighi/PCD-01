package pcd.poool.model;

import java.util.List;

public interface BoardObserver {
    void modelUpdated(List<Ball> ballList, Ball playerBall, int framePerSec);
}
