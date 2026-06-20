package pcd.pooolThreadOriented.test;

import pcd.pooolThreadOriented.model.*;
import pcd.pooolThreadOriented.util.Latch;
import pcd.pooolThreadOriented.util.SynchCell;

import java.util.List;
import java.util.function.Consumer;

public class SimulationCoordinator extends Thread {

	private final Board board;
	private final GameState gameState;
	private final List<BoardObserver> observers;
	private final List<SynchCell<Runnable>> workBuffer;
	private final Latch workLatch;
	private final SpatialGrid grid;

	public SimulationCoordinator(
			Board board,
			List<BoardObserver> observers,
			List<SynchCell<Runnable>> workBuffer,
			Latch workLatch
	) {
		this.board = board;
		this.gameState = board.getState();
		this.observers = List.copyOf(observers);
		this.workBuffer = workBuffer;
		this.workLatch = workLatch;
		double maxSmallRadius = 0.0;
		for (Ball b : gameState.getSmallBalls()) {
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
			} else if (nTicks > WARMUP_FRAMES + MEASURE_FRAMES) {
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
		// Chiude i cell dei worker per evitare il deadlock di shutdown
		// (worker bloccati in workCell.get() senza nessuno che faccia put).
		for (var cell : workBuffer) {
			cell.end();
		}
		for (var o : observers) {
			o.gameOver(board.getBoardViewInfo(), gameState.getGameStateViewInfo(), tickPerSec, gameState.getGameResult());
		}
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
		final int nActualWorker = Math.min(workBuffer.size(), totalRows);

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

		for (Ball mainBall : mainBalls) {
			for (Ball otherBall : allBalls) {
				if (mainBall.getId() != otherBall.getId()) {
					Ball.resolveCollision(mainBall, otherBall);
				}
			}
		}
	}

    public void distributeWork(Consumer<Integer> action, int nActualWorker) {
		workLatch.reset(nActualWorker);
        for (int i = 0; i < nActualWorker; i++) {
            int workerIndex = i;
			workBuffer.get(i).put(
					() -> action.accept(workerIndex)
			);
        }
		workLatch.await();
    }

	public void distributeLinearWork(Consumer<Ball> action) {
		var allBalls = gameState.getAllBalls();
		int totalSize = allBalls.size();

		int nActualWorker = Math.min(workBuffer.size(), totalSize);
		int workAmount = totalSize / nActualWorker;

        distributeWork(workerIndex -> {
            int start = workerIndex * workAmount;
            // L'ultimo worker prende tutti gli elementi fino alla fine della lista,
            // includendo il resto della divisione
            int end = (workerIndex == nActualWorker - 1) ? totalSize : start + workAmount;
            for (int j = start; j < end; j++) {
                action.accept(allBalls.get(j));
            }
        }, nActualWorker);
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
