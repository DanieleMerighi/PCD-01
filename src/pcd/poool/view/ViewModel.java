package pcd.poool.view;

import pcd.poool.model.Ball;
import pcd.poool.model.P2d;

import java.util.ArrayList;
import java.util.List;

record BallViewInfo(P2d pos, double radius) {}

public class ViewModel {

	private final ArrayList<BallViewInfo> balls;
	private BallViewInfo player;
	private int framePerSec;
	
	public ViewModel() {
		balls = new ArrayList<>();
		framePerSec = 0;
	}
	
	public synchronized void update(List<Ball> ballList, Ball playerBall, int framePerSec) {
		this.balls.clear();
		for (var ball: ballList) {
			this.balls.add(new BallViewInfo(ball.getPos(), ball.getRadius()));
		}
		this.framePerSec = framePerSec;
		this.player = new BallViewInfo(playerBall.getPos(), playerBall.getRadius());
	}

	synchronized ArrayList<BallViewInfo> getBalls(){
		return new ArrayList<>(balls);
	}

	public synchronized int getFramePerSec() {
		return framePerSec;
	}

	synchronized BallViewInfo getPlayerBall() {
		return player;
	}
	
}
