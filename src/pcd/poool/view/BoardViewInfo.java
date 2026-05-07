package pcd.poool.view;

import java.util.List;

public record BoardViewInfo(
		BallViewInfo humanBall,
		BallViewInfo botBall,
		List<HoleViewInfo> holes
) {}
