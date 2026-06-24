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
		board.applyHumanKick();
		board.applyBotKick();

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

		// ---------------------------------------------------------
		// PASSATA 1: Collisioni Righe PARI (0, 2, 4, 6...)
		// ---------------------------------------------------------
		distributeWork(workerIndex -> {
			for (int r = 0; r < totalRows; r += 2) {
				// Interleavizzazione logica delle sole righe pari tra i worker
				if ((r / 2) % nActualWorker == workerIndex) {
					processRowCollisions(r);
				}
			}
		}, nActualWorker);

		// ---------------------------------------------------------
		// PASSATA 2: Collisioni Righe DISPARI (1, 3, 5, 7...)
		// ---------------------------------------------------------
		distributeWork(workerIndex -> {
			for (int r = 1; r < totalRows; r += 2) {
				// Interleavizzazione logica delle sole righe dispari tra i worker
				if ((r / 2) % nActualWorker == workerIndex) {
					processRowCollisions(r);
				}
			}
		}, nActualWorker);
	}

	// Metodo di supporto per calcolare le collisioni intra e inter cella di una riga
	private void processRowCollisions(int r) {
		final int totalCols = grid.getCols();
		for (int c = 0; c < totalCols; c++) {
			List<Ball> cellBalls = grid.getCell(c, r);
			if (cellBalls.isEmpty()) continue;

			// 1. Collisioni INTRA-cella
			int cSize = cellBalls.size();
			for (int i = 0; i < cSize; i++) {
				Ball b1 = cellBalls.get(i);
				for (int j = i + 1; j < cSize; j++) {
					Ball.resolveCollision(b1, cellBalls.get(j));
				}
			}

			// 2. Collisioni INTER-cella (con i 4 vicini in avanti/basso)
			List<Ball> nearbyBalls = grid.getForwardNeighbors(c, r);
			for (int i = 0; i < cSize; i++) {
				Ball b1 = cellBalls.get(i);
				for (var b2 : nearbyBalls) {
					Ball.resolveCollision(b1, b2);
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
		var allBalls = board.getAllBalls();
		int totalSize = allBalls.size();

		int nActualWorker = Math.min(workBuffer.size(), totalSize);
		int workAmount = totalSize / nActualWorker;

		distributeWork(workerIndex -> {
			int start = workerIndex * workAmount;
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
