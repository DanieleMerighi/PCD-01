package pcd.pooolTaskOriented.test;

import pcd.pooolTaskOriented.model.*;
import pcd.pooolTaskOriented.util.IntRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class SimulationCoordinator extends Thread {

	private final Board board;
	private final GameState gameState;
	private final List<BoardObserver> observers;
	private final ExecutorService exec;
	private final int nTasks;
	private final SpatialGrid grid;

	private double medianTimeMs = 0.0;
	private double meanTimeMs = 0.0;

	public SimulationCoordinator(
			Board board,
			List<BoardObserver> observers,
			int nWorker,
			int nTasks
	) {
		this.board = board;
		this.gameState = board.getState();
		this.observers = List.copyOf(observers);
		this.exec = Executors.newFixedThreadPool(nWorker);
		this.nTasks = nTasks;
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

		// Finestra di campionamento tarata per la JIT C2
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
		exec.shutdown();
	}

	private void processStatistics(long[] timesNano) {
		long totalNano = 0;
		for (long t : timesNano) { totalNano += t; }
		this.meanTimeMs = (totalNano / 1_000_000.0) / timesNano.length;

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

		distributeLinearTasks(board.getAllBalls(), ball -> {
			ball.updateState(dt, board);
			for (var hole : board.getHoles()) {
				Ball.resolveHole(ball, hole, board, gameState);
			}
		}, nTasks);

		if (gameState.isGameOver())
			return;

		if (board.isSmallBallEmpty()) {
			setEndGame();
			return;
		}

		grid.clearAndPopulate(board.getAllBalls(), board.getBounds());
		final int totalRows = grid.getRows();

		// Passata PARI
		runTasks(
				IntRange.withStep(0, totalRows, 2),
				this::processRowCollisions
		);

		// Passata DISPARI
		runTasks(
				IntRange.withStep(1, totalRows, 2),
				this::processRowCollisions
		);
	}

	private void processRowCollisions(int r) {
		final int totalCols = grid.getCols();
		for (int c = 0; c < totalCols; c++) {
			List<Ball> cellBalls = grid.getCell(c, r);
			if (cellBalls.isEmpty()) continue;

			int cSize = cellBalls.size();
			for (int i = 0; i < cSize; i++) {
				Ball b1 = cellBalls.get(i);
				for (int j = i + 1; j < cSize; j++) {
					Ball.resolveCollision(b1, cellBalls.get(j));
				}
			}

			List<Ball> nearbyBalls = grid.getForwardNeighbors(c, r);
			for (int i = 0; i < cSize; i++) {
				Ball b1 = cellBalls.get(i);
				for (var b2 : nearbyBalls) {
					Ball.resolveCollision(b1, b2);
				}
			}
		}
	}

	public <T> void runTasks(List<T> items, Consumer<T> action) {
		var work = new ArrayList<Callable<Void>>(items.size());
		for (var item : items) {
			work.add(() -> {
				action.accept(item);
				return null;
			});
		}
		try {
			exec.invokeAll(work);
		} catch (Exception ignored) {}
	}

	public <T> void distributeLinearTasks(List<T> items, Consumer<T> action, int nTasks) {
		int totalSize = items.size();
		int actualTasks = Math.min(nTasks, totalSize);
		int workAmount = totalSize / actualTasks;

		runTasks(IntRange.until(actualTasks), taskIndex -> {
			int start = taskIndex * workAmount;
			int end = (taskIndex == actualTasks - 1) ? totalSize : start + workAmount;
			for (int j = start; j < end; j++) {
				action.accept(items.get(j));
			}
		});
	}

	private void setEndGame() {
		int humanScore = gameState.getHumanScore();
		int botScore = gameState.getBotScore();
		String gameResult = humanScore > botScore ? "Human wins! " + humanScore + " - " + botScore
				: botScore > humanScore ? "Bot wins! " + botScore + " - " + humanScore
				: "Draw! " + humanScore + " - " + botScore;
		gameState.endGame(gameResult);
	}

	private void notifyObservers(long tickPerSec) {
		for (var o: observers) {
			o.modelUpdated(board.getBoardViewInfo(), gameState.getGameStateViewInfo(), tickPerSec);
		}
	}

}
