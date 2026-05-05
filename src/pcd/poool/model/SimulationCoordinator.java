package pcd.poool.model;

import pcd.poool.util.*;

import java.util.ArrayList;
import java.util.List;

public class SimulationCoordinator extends Thread {

	private final Board board;
	private final GameState gameState;
	private final List<BoardObserver> observers;
	private final WorkBuffer workBuffer;
	
	public SimulationCoordinator(
			Board board,
			GameState gameState,
			List<BoardObserver> observers,
			WorkBuffer workBuffer
	) {
		this.board = board;
		this.gameState = gameState;
		this.observers = new ArrayList<>(observers);
		this.workBuffer = workBuffer;
	}

	@Override
	public void run() {
		long nFrames = 0;
		long t0 = System.currentTimeMillis();
		long lastUpdateTime = System.currentTimeMillis();
		while (!gameState.isGameOver()) {
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
			o.gameOver(gameState.getGameResult());
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
			workBuffer.put(() -> Ball.resolveHole(playerBall, hole, board, gameState));
			workBuffer.put(() -> Ball.resolveHole(botBall, hole, board, gameState));
			for (var b : board.getBalls()) {
				workBuffer.put(() -> Ball.resolveHole(b, hole, board, gameState));
			}
		}
		workBuffer.waitAll();

		var balls = board.getBalls();
		if (balls.isEmpty()) {
			int playerScore = gameState.getPlayerScore();
			int botScore = gameState.getBotScore();
			String gameResult = playerScore > botScore ? "Player wins! " + playerScore + " - " + botScore
					: botScore > playerScore ? "Bot wins! " + botScore + " - " + playerScore
					: "Draw! " + playerScore + " - " + botScore;
			gameState.endGame(gameResult);
			return;
		}

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
			o.modelUpdated(board.getBoardViewInfo(), gameState.getGameStateViewInfo(), framePerSec);
		}
	}
}
