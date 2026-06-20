package pcd.pooolTaskOriented.model;

import pcd.pooolTaskOriented.util.*;

import java.util.ArrayList;
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
	private final int nWorker;
	private final SpatialGrid grid;

	public SimulationCoordinator(
			Board board,
			List<BoardObserver> observers,
			int nWorker
	) {
		this.board = board;
		this.gameState = board.getState();
		this.observers = List.copyOf(observers);
		this.exec = Executors.newFixedThreadPool(nWorker);
		this.nWorker = nWorker;
		double maxSmallRadius = 0.0;
		for (Ball b : gameState.getSmallBalls()) {
			if (b.getRadius() > maxSmallRadius) {
				maxSmallRadius = b.getRadius();
			}
		}
		this.grid = new SpatialGrid(board.getBounds(), maxSmallRadius);
	}

	@Override
	public void run() {
		long nTicks = 0;
		long t0 = System.currentTimeMillis();
		long lastUpdateTime = System.currentTimeMillis();
		long tickPerSec = 0;
		while (!gameState.isGameOver()) {
			long elapsed = System.currentTimeMillis() - lastUpdateTime;
			lastUpdateTime = System.currentTimeMillis();

			this.updateState(elapsed);

			nTicks++;
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
		exec.shutdown();
	}

	private void updateState(long dt) {
		distributeLinearWork(ball -> ball.updateState(dt, board));

		distributeLinearWork(ball -> {
			for (var hole : board.getHoles()) {
				Ball.resolveHole(ball, hole, gameState);
			}
		});

		if (gameState.isGameOver())
			return;

		if (gameState.isSmallBallEmpty()) {
			setEndGame();
			return;
		}

		grid.clearAndPopulate(gameState.getSmallBalls(), board.getBounds());

		final int totalRows = grid.getRows();
		final int nActualWorker = Math.min(nWorker, totalRows);

		// Distribute work to check small balls collisions
		distributeWork(workerIndex -> {
			// Il worker elabora una riga e poi salta di nActualWorker righe (round robin)
			for (int r = workerIndex; r < grid.getRows(); r += nActualWorker) {
				for (int c = 0; c < grid.getCols(); c++) {
					List<Ball> cellBalls = grid.getCell(c, r);
					if (cellBalls.isEmpty()) continue;

					List<Ball> nearbyBalls = grid.getNearbyBalls(c, r);

					for (Ball b1 : cellBalls) {
						for (Ball b2 : nearbyBalls) {
							if (b1.getId() < b2.getId()) {
								Ball.resolveCollision(b1, b2);
							}
						}
					}
				}
			}
		}, nActualWorker);

		var mainBalls = gameState.getMainBalls();
		var allBalls = gameState.getAllBalls();

		// Sequentially check Collisions between main balls and small balls
		for (Ball mainBall : mainBalls) {
			for (Ball otherBall : allBalls) {
				if (mainBall.getId() != otherBall.getId()) {
					Ball.resolveCollision(mainBall, otherBall);
				}
			}
		}
	}

	public void distributeWork(Consumer<Integer> action, int nActualWorker) {
		var work = new ArrayList<Callable<Void>>();
		for (int i = 0; i < nActualWorker; i++) {
			int workerIndex = i;
			work.add(() -> {
				action.accept(workerIndex);
				return null;
			});
		}
		try {
			exec.invokeAll(work);
		} catch (Exception ignored) {}

	}

	public void distributeLinearWork(Consumer<Ball> action) {
		var allBalls = gameState.getAllBalls();
		int totalSize = allBalls.size();

		int actualWorkers = Math.min(nWorker, totalSize);
		int workAmount = totalSize / actualWorkers;

		distributeWork(workerIndex -> {
			int start = workerIndex * workAmount;
			// L'ultimo worker prende tutti gli elementi fino alla fine della lista,
			// includendo il resto della divisione
			int end = (workerIndex == actualWorkers - 1) ? totalSize : start + workAmount;
			for (int j = start; j < end; j++) {
				action.accept(allBalls.get(j));
			}
		}, nWorker);
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
