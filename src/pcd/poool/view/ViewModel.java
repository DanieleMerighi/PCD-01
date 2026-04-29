package pcd.poool.view;

import pcd.poool.model.Board;
import pcd.poool.model.P2d;

import java.util.ArrayList;

record BallViewInfo(P2d pos, double radius) {}

public class ViewModel {

	private final ArrayList<BallViewInfo> balls;
	private BallViewInfo player;
	private int framePerSec;
	
	public ViewModel() {
		balls = new ArrayList<>();
		framePerSec = 0;
	}
	
	public synchronized void update(Board board, int framePerSec) {
		balls.clear();
		for (var b: board.getBalls()) {
			balls.add(new BallViewInfo(b.getPos(), b.getRadius()));
		}
		this.framePerSec = framePerSec;
		var p = board.getPlayerBall();
		player = new BallViewInfo(p.getPos(), p.getRadius());
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
