package pcd.poool.view;

import java.util.ArrayList;

public record BoardViewInfo(BallViewInfo player, ArrayList<BallViewInfo> balls, ArrayList<HoleViewInfo> holes, int playerScore, int botScore) {
}
