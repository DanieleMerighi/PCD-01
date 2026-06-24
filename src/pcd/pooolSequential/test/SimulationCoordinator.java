package pcd.pooolSequential.test;

import pcd.pooolSequential.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimulationCoordinator extends Thread {

	private final Board board;
	private final GameState gameState;
	private final List<BoardObserver> observers;
	private final SpatialGrid grid;

	// Metriche registrate
	private double medianTimeMs = 0.0;
	private double meanTimeMs = 0.0;

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

	public double getMedianTimeMs() { return medianTimeMs; }
	public double getMeanTimeMs() { return meanTimeMs; }

	@Override
	public void run() {
		long nTicks = 0;
		long t0 = System.currentTimeMillis();
		long lastUpdateTime = t0;
		long tickPerSec = 0;

		// Parametri di benchmark rigorosi
		final int WARMUP_FRAMES = 10000;
		final int MEASURE_FRAMES = 20000;
		long[] frameTimesNano = new long[MEASURE_FRAMES];
		int measureIndex = 0;

		while (!gameState.isGameOver()) {
			long now = System.currentTimeMillis();
			long elapsed = now - lastUpdateTime;
			lastUpdateTime = now;

			long startUpdate = System.nanoTime();
			this.updateState(elapsed);
			long endUpdate = System.nanoTime();

			nTicks++;

			// Registrazione campioni post-warmup
			if (nTicks > WARMUP_FRAMES) {
				frameTimesNano[measureIndex++] = (endUpdate - startUpdate);

				if (measureIndex >= MEASURE_FRAMES) {
					processStatistics(frameTimesNano);
					break;
				}
			}

			tickPerSec = 0;
			long dt = (System.currentTimeMillis() - t0);
			if (dt > 0) {
				tickPerSec = nTicks * 1000 / dt;
			}
			notifyObservers(tickPerSec);
		}
		for (var o : observers) {
			o.gameOver(board.getBoardViewInfo(), gameState.getGameStateViewInfo(), tickPerSec, gameState.getGameResult());
		}
	}

	private void processStatistics(long[] timesNano) {
		// Calcolo Media
		long totalNano = 0;
		for (long t : timesNano) { totalNano += t; }
		this.meanTimeMs = (totalNano / 1_000_000.0) / timesNano.length;

		// Calcolo Mediana (P50)
		Arrays.sort(timesNano);
		if (timesNano.length % 2 == 0) {
			this.medianTimeMs = ((timesNano[timesNano.length / 2] + timesNano[(timesNano.length / 2) - 1]) / 2.0) / 1_000_000.0;
		} else {
			this.medianTimeMs = (timesNano[timesNano.length / 2]) / 1_000_000.0;
		}
	}

	private void updateState(long dt) {
		board.applyHumanKick();
		board.applyBotKick();

		var allBalls = board.getAllBalls();
		for (var ball : allBalls) {
			ball.updateState(dt, board);
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
			processRowCollisions(r);
		}
	}

	private void processRowCollisions(int r) {
		for (int c = 0; c < grid.getCols(); c++) {
			List<Ball> cellBalls = grid.getCell(c, r);
			if (cellBalls.isEmpty()) continue;

			// 1. Collisioni INTRA-cella
			for (int i = 0; i < cellBalls.size(); i++) {
				Ball b1 = cellBalls.get(i);
				for (int j = i + 1; j < cellBalls.size(); j++) {
					Ball.resolveCollision(b1, cellBalls.get(j));
				}
			}

			// 2. Collisioni INTER-cella (con i 4 vicini in avanti/basso)
			List<Ball> nearbyBalls = grid.getForwardNeighbors(c, r);
			for (int i = 0; i < cellBalls.size(); i++) {
				Ball b1 = cellBalls.get(i);
				for (Ball b2 : nearbyBalls) {
					Ball.resolveCollision(b1, b2);
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