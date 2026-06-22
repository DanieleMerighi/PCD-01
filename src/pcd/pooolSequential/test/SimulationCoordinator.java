package pcd.pooolSequential.test;

import pcd.pooolSequential.model.*;

import java.util.ArrayList;
import java.util.List;

public class SimulationCoordinator extends Thread {

	private final Board board;
	private final GameState gameState;
	private final List<BoardObserver> observers;
	private final SpatialGrid grid;
	
	public SimulationCoordinator(Board board, List<BoardObserver> observers) {
		this.board = board;
		this.gameState = board.getState();
		this.observers = new ArrayList<>(observers);
		double maxSmallRadius = 0.0;
		for (Ball b : board.getAllBalls()) {
			if (b.getRadius() > maxSmallRadius) {
				maxSmallRadius = b.getRadius();
			}
		}
		this.grid = new SpatialGrid(board.getBounds(), maxSmallRadius);
	}

	private double averageTimeMs = 0.0;

	public double getAverageTimeMs() {
		return averageTimeMs;
	}

	@Override
	public void run() {
		long nTicks = 0;
		long t0 = System.currentTimeMillis();
		long lastUpdateTime = System.currentTimeMillis();
		long tickPerSec = 0;

		final int WARMUP_FRAMES = 1000;
		final int MEASURE_FRAMES = 2000;
		long totalMeasureTimeNano = 0;
		int measuredFramesCount = 0;

		while (!gameState.isGameOver()) {
			long elapsed = System.currentTimeMillis() - lastUpdateTime;
			lastUpdateTime = System.currentTimeMillis();

			long startUpdate = System.nanoTime();
			this.updateState(elapsed);
			long endUpdate = System.nanoTime();

			nTicks++;

			if (nTicks > WARMUP_FRAMES && nTicks <= (WARMUP_FRAMES + MEASURE_FRAMES)) {
				totalMeasureTimeNano += (endUpdate - startUpdate);
				measuredFramesCount++;
			} else if (nTicks > (WARMUP_FRAMES + MEASURE_FRAMES)) {
				averageTimeMs = (totalMeasureTimeNano / 1_000_000.0) / measuredFramesCount;
				break;
			}

			tickPerSec = 0;
			long dt = (System.currentTimeMillis() - t0);
			if (dt > 0) {
				tickPerSec = nTicks*1000/dt;
			}
			notifyObservers(tickPerSec);
		}
		for (var o : observers) {
			o.gameOver(board.getBoardViewInfo(), gameState.getGameStateViewInfo(), tickPerSec, gameState.getGameResult());
		}
	}

	private void updateState(long dt) {
		var allBalls = board.getAllBalls();

		for (var b : allBalls) {
			b.updateState(dt, board);
		}

		for (var ball : allBalls) {
			for (var hole : board.getHoles()) {
				Ball.resolveHole(ball, hole, board, gameState);
			}
		}

		if (gameState.isGameOver())
			return;

		if (board.isSmallBallEmpty()) {
			setEndGame();
			return;
		}

		grid.clearAndPopulate(board.getAllBalls(), board.getBounds());

		for (int r = 0; r < grid.getRows(); r++) {
			for (int c = 0; c < grid.getCols(); c++) {
				List<Ball> cellBalls = grid.getCell(c, r);
				if (cellBalls.isEmpty()) continue;

				// 1. Collisioni INTRA-cella (tra palline dentro la stessa cella)
				for (int i = 0; i < cellBalls.size(); i++) {
					Ball b1 = cellBalls.get(i);
					for (int j = i + 1; j < cellBalls.size(); j++) {
						Ball.resolveCollision(b1, cellBalls.get(j));
					}
				}

				// 2. Collisioni INTER-cella (con le 4 celle adiacenti)
				List<Ball> nearbyBalls = grid.getForwardNeighbors(c, r);
				for (Ball b1 : cellBalls) {
					for (Ball b2 : nearbyBalls) {
						Ball.resolveCollision(b1, b2); // Rimosso il controllo ID
					}
				}
			}
		}
	}

	private void setEndGame() {
		int humanScore = gameState.getHumanScore();
		int botScore = gameState.getBotScore();
		String gameResult = humanScore > botScore ? "Human wins! " + humanScore + " - " + botScore
				: botScore > humanScore ? "Bot wins! " + botScore + " - " + humanScore
				: "Draw! " + humanScore + " - " + botScore;
		gameState.endGame(gameResult);
	}

	private void notifyObservers(long framePerSec) {
		for (var o: observers) {
			o.modelUpdated(board.getBoardViewInfo(), gameState.getGameStateViewInfo(), framePerSec);
		}
	}

}
