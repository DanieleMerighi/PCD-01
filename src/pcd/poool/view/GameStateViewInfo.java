package pcd.poool.view;

import java.util.List;

public record GameStateViewInfo(
		List<BallViewInfo> balls,
		int humanScore,
		int botScore
) {}
