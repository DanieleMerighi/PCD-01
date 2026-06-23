package pcd.pooolThreadOriented.model;

import pcd.pooolThreadOriented.util.*;

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
		for (Ball b : board.getAllBalls()) {
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
		for (var cell : workBuffer) {
			cell.end();
		}
		for (var o : observers) {
			o.gameOver(board.getBoardViewInfo(), gameState.getGameStateViewInfo(), tickPerSec, gameState.getGameResult());
		}
	}

	private void updateState(long dt) {
		distributeLinearWork(ball -> {
			ball.updateState(dt, board);
			for (var hole : board.getHoles()) {
				Ball.resolveHole(ball, hole, board, gameState);
			}
		});

		if (gameState.isGameOver())
			return;

		if (board.isSmallBallEmpty()) {
			setEndGame();
			return;
		}

		grid.clearAndPopulate(board.getAllBalls(), board.getBounds());

		final int totalRows = grid.getRows();
		final int nActualWorker = Math.min(workBuffer.size(), totalRows);

		// Distribute work to check small balls collisions
		distributeWork(workerIndex -> {
			for (int r = workerIndex; r < grid.getRows(); r += nActualWorker) {
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
		}, nActualWorker);
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
		var allBalls = board.getAllBalls();
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
