package pcd.pooolTaskOriented.view;

import java.util.List;

public record BoardViewInfo(
		BallViewInfo humanBall,
		BallViewInfo botBall,
		List<BallViewInfo> smallBalls,
		List<HoleViewInfo> holes
) {}
