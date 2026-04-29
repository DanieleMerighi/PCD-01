package pcd.poool.view;

import java.util.ArrayList;

public record BoardViewInfo(BallViewInfo player, BallViewInfo bot, ArrayList<BallViewInfo> balls, ArrayList<HoleViewInfo> holes, int playerScore, int botScore) {
}
