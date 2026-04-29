package pcd.poool.view;

import pcd.poool.model.Ball;
import pcd.poool.model.Hole;
import pcd.poool.model.P2d;

import java.util.ArrayList;
import java.util.List;

record BallViewInfo(P2d pos, double radius) {}
record HoleViewInfo(P2d pos, double radius) {}

public class ViewModel {

	private final ArrayList<BallViewInfo> balls;
	private BallViewInfo player;
	private int framePerSec;
	private final ArrayList<HoleViewInfo> holes;
	
	public ViewModel() {
		balls = new ArrayList<>();
		framePerSec = 0;
		this.holes = new ArrayList<>();
	}
	
	public synchronized void update(List<Ball> ballList, Ball playerBall, List<Hole> holeList, int framePerSec) {
		this.balls.clear();
		for (var ball: ballList) {
			this.balls.add(new BallViewInfo(ball.getPos(), ball.getRadius()));
		}
		this.framePerSec = framePerSec;
		this.player = new BallViewInfo(playerBall.getPos(), playerBall.getRadius());
		this.holes.clear();
		for (var hole: holeList) {
			this.holes.add(new HoleViewInfo(hole.pos(), hole.radius()));
		}
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

	synchronized ArrayList<HoleViewInfo> getHoles() {
		return new ArrayList<>(holes);
	}
	
}
