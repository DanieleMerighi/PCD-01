package pcd.pooolThreadOriented.test;

import pcd.pooolThreadOriented.model.*;
import pcd.pooolThreadOriented.util.Latch;
import pcd.pooolThreadOriented.util.SynchCell;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class SimulationCoordinator extends Thread {

	private final Board board;
	private final GameState gameState;
	private final List<BoardObserver> observers;
	private final List<SynchCell<Runnable>> workBuffer;
	private final Latch workLatch;
	private final SpatialGrid grid;

	private double medianTimeMs = 0.0;
	private double meanTimeMs = 0.0;

	public SimulationCoordinator(Board board, List<BoardObserver> observers, List<SynchCell<Runnable>> workBuffer, Latch workLatch) {
		this.board = board;
		this.gameState = board.getState();
		this.observers = List.copyOf(observers);
		this.workBuffer = workBuffer;
		this.workLatch = workLatch;
		double maxSmallRadius = 0.0;
		for (Ball b : board.getAllBalls()) {
			if (b.getRadius() > maxSmallRadius) { maxSmallRadius = b.getRadius(); }
		}
		this.grid = new SpatialGrid(board.getBounds(), maxSmallRadius);
	}

	public double getMedianTimeMs() { return medianTimeMs; }
	public double getMeanTimeMs() { return meanTimeMs; }

	@Override
	public void run() {
		long nTicks = 0;
		// Aumentato il warmup per saturare il compilatore JIT C2
		final int WARMUP_FRAMES = 10000;
		final int MEASURE_FRAMES = 20000;

		// Pre-allocazione del buffer per i campioni (Zero-GC overhead)
		long[] frameTimesNano = new long[MEASURE_FRAMES];
		int measureIndex = 0;

		long lastFrameTime = System.nanoTime();

		while (!gameState.isGameOver()) {
			long now = System.nanoTime();
			long elapsedMs = (now - lastFrameTime) / 1_000_000;
			lastFrameTime = now;

			this.updateState(elapsedMs);

			nTicks++;

			if (nTicks > WARMUP_FRAMES) {
				long frameDuration = System.nanoTime() - now;
				frameTimesNano[measureIndex++] = frameDuration;

				if (measureIndex >= MEASURE_FRAMES) {
					processStatistics(frameTimesNano);
					break;
				}
			}
		}

		for (var cell : workBuffer) { cell.end(); }
	}

	private void processStatistics(long[] timesNano) {
		// 1. Calcolo Media Aritmetica
		long totalNano = 0;
		for (long t : timesNano) { totalNano += t; }
		this.meanTimeMs = (totalNano / 1_000_000.0) / timesNano.length;

		// 2. Calcolo Mediana (P50) - Ignora i picchi anomali dovuti al GC
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
