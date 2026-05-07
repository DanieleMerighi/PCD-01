package pcd.poool.view;

import java.util.List;

public record BoardViewInfo(
		BallViewInfo player,
		BallViewInfo bot,
		List<HoleViewInfo> holes
) {}
