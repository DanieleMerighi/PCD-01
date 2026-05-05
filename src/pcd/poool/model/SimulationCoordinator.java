package pcd.poool.model;

import pcd.poool.util.*;

import java.util.ArrayList;
import java.util.List;

public class SimulationCoordinator extends Thread {

	private final Board board;
	private final List<BoardObserver> observers;
	private final WorkBuffer workBuffer;
	
	public SimulationCoordinator(
			Board board,
			List<BoardObserver> observers,
			WorkBuffer workBuffer
	) {
		this.board = board;
		this.observers = new ArrayList<>(observers);
		this.workBuffer = workBuffer;
	}

	@Override
	public void run() {
		long nFrames = 0;
		long t0 = System.currentTimeMillis();
		long lastUpdateTime = System.currentTimeMillis();
		while (!board.isGameOver()) {
			long elapsed = System.currentTimeMillis() - lastUpdateTime;
			lastUpdateTime = System.currentTimeMillis();
			this.updateState(elapsed);

			nFrames++;
			long framePerSec = 0;
			long dt = (System.currentTimeMillis() - t0);
			if (dt > 0) {
				framePerSec = nFrames*1000/dt;
			}
			notifyObservers(framePerSec);
		}
		for (var o : observers) {
			o.gameOver(board.getGameResult());
		}
	}

	private void updateState(long dt) {
		var playerBall = board.getPlayerBall();
		var botBall = board.getBotBall();

		workBuffer.put(() -> playerBall.updateState(dt, board));
		workBuffer.put(() -> botBall.updateState(dt, board));
		for (var b : board.getBalls()) {
			workBuffer.put(() -> b.updateState(dt, board));
		}
		workBuffer.waitAll();

		for (var hole : board.getHoles()) {
			workBuffer.put(() -> Ball.resolveHole(playerBall, hole, board));
			workBuffer.put(() -> Ball.resolveHole(botBall, hole, board));
			for (var b : board.getBalls()) {
				workBuffer.put(() -> Ball.resolveHole(b, hole, board));
			}
		}
		workBuffer.waitAll();

		/*
		if (balls.isEmpty()) {
			gameOver = true;
			gameResult = playerScore > botScore ? "Player wins! " + playerScore + " - " + botScore
					: botScore > playerScore ? "Bot wins! " + botScore + " - " + playerScore
					: "Draw! " + playerScore + " - " + botScore;
		}
	 	*/

		var balls = board.getBalls();
		for (int i = 0; i < balls.size() - 1; i++) {
			int finalI = i;
			workBuffer.put(() -> {
				for (int j = finalI + 1; j < balls.size(); j++) {
					Ball.resolveCollision(balls.get(finalI), balls.get(j));
				}
			});
		}
		for (var b: balls) {
			workBuffer.put(() -> Ball.resolveCollision(playerBall, b));
			workBuffer.put(() -> Ball.resolveCollision(botBall, b));
		}
		workBuffer.put(() -> Ball.resolveCollision(playerBall, botBall));
		workBuffer.waitAll();
	}

	private void notifyObservers(long framePerSec) {
		for (var o: observers) {
			o.modelUpdated(board.getBoardViewInfo(), framePerSec);
		}
	}
}
