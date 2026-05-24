package pcd.pooolThreadOriented.test;

import pcd.pooolThreadOriented.model.Ball;
import pcd.pooolThreadOriented.model.Board;
import pcd.pooolThreadOriented.model.BoardObserver;
import pcd.pooolThreadOriented.model.GameState;
import pcd.pooolThreadOriented.util.Latch;
import pcd.pooolThreadOriented.util.SynchBox;

import java.util.List;
import java.util.function.Consumer;

public class SimulationCoordinator extends Thread {

	private final Board board;
	private final GameState gameState;
	private final List<BoardObserver> observers;
	private final List<SynchBox<Runnable>> workBuffer;
	private final Latch workLatch;
	
	public SimulationCoordinator(
			Board board,
			List<BoardObserver> observers,
			List<SynchBox<Runnable>> workBuffer,
			Latch workLatch
	) {
		this.board = board;
		this.gameState = board.getState();
		this.observers = List.copyOf(observers);
		this.workBuffer = workBuffer;
		this.workLatch = workLatch;
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

		var smallBalls = gameState.getSmallBalls();
		if (smallBalls.isEmpty()) {
			setEndGame();
			return;
		}

		var allBalls = gameState.getAllBalls();
		int nActualWorker = Math.min(workBuffer.size(), allBalls.size());

        distributeWork(workerIndex -> {
            for (int i = workerIndex; i < allBalls.size() - 1; i += nActualWorker) {
                for (int j = i + 1; j < allBalls.size(); j++) {
                    Ball.resolveCollision(allBalls.get(i), allBalls.get(j));
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
