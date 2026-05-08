package pcd.pooolThreadOriented.model;

import pcd.pooolThreadOriented.util.*;

import java.util.List;
import java.util.function.Consumer;

public class SimulationCoordinator extends Thread {

	private final Board board;
	private final GameState gameState;
	private final List<BoardObserver> observers;
	private final WorkBuffer workBuffer;
	private final int nWorker;
	
	public SimulationCoordinator(
			Board board,
			List<BoardObserver> observers,
			WorkBuffer workBuffer,
			int nWorker
	) {
		this.board = board;
		this.gameState = board.getState();
		this.observers = List.copyOf(observers);
		this.workBuffer = workBuffer;
		this.nWorker = nWorker;
	}

	@Override
	public void run() {
		long nTicks = 0;
		long t0 = System.currentTimeMillis();
		long lastUpdateTime = System.currentTimeMillis();
		while (!gameState.isGameOver()) {
			long elapsed = System.currentTimeMillis() - lastUpdateTime;
			lastUpdateTime = System.currentTimeMillis();

			this.updateState(elapsed);

			nTicks++;
			long tickPerSec = 0;
			long dt = (System.currentTimeMillis() - t0);
			if (dt > 0) {
				tickPerSec = nTicks*1000/dt;
			}
			notifyObservers(tickPerSec);
		}
		for (var o : observers) {
			o.gameOver(gameState.getGameResult());
		}
	}

	private void updateState(long dt) {
		distributeWork(ball -> ball.updateState(dt, board));
		workBuffer.waitAll();

		distributeWork(ball -> {
			for (var hole : board.getHoles()) {
				Ball.resolveHole(ball, hole, gameState);
			}
		});
		workBuffer.waitAll();

		if (gameState.isGameOver())
			return;

		var balls = gameState.getSmallBalls();
		if (balls.isEmpty()) {
			setEndGame();
			return;
		}

		var allBalls = gameState.getAllBalls();
		for (int i = 0; i < allBalls.size() - 1; i++) {
			int finalI = i;
			workBuffer.put(() -> {
				for (int j = finalI + 1; j < allBalls.size(); j++) {
					Ball.resolveCollision(allBalls.get(finalI), allBalls.get(j));
				}
			});
		}
		workBuffer.waitAll();
	}

	public void distributeWork(Consumer<Ball> action) {
		var allBalls = gameState.getAllBalls();
		int totalSize = allBalls.size();
		if (totalSize == 0 || nWorker <= 0) return;

		int actualWorkers = Math.min(nWorker, totalSize);
		int workAmount = totalSize / actualWorkers;

		for (int i = 0; i < actualWorkers; i++) {
			int start = i * workAmount;
			// L'ultimo worker prende tutti gli elementi fino alla fine della lista,
			// includendo il resto della divisione
			int end = (i == actualWorkers - 1) ? totalSize : start + workAmount;
			workBuffer.put(() -> {
				for (int j = start; j < end; j++) {
					action.accept(allBalls.get(j));
				}
			});
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

	private void notifyObservers(long tickPerSec) {
		for (var o: observers) {
			o.modelUpdated(board.getBoardViewInfo(), gameState.getGameStateViewInfo(), tickPerSec);
		}
	}

}


/*
list.allBalls
 1  2   3  4 5  6   7  8  9
    20 30 40 50 60 70 80 90  1  list.allBalls
       31 41 51 61 71 81 91  2
          42 52 62 72 82 92  3
             5 6 7 8 9       4
               6 7 8 9       5
				 7 8 9       6
				   8 9       7
					 9       8
							 9
 */

/*
var balls = board.getAllBalls();
		int low = 0;
		int high = balls.size() - 1;
		while (low <= high) {
			int finalLow = low;
			int finalHigh = high;
			workBuffer.put(() -> {
				for (int j = finalLow + 1; j < balls.size(); j++) {
					Ball.resolveCollision(balls.get(finalLow), balls.get(j));
				}
				if (finalLow != finalHigh) {
					for (int j = finalHigh + 1; j < balls.size(); j++) {
						Ball.resolveCollision(balls.get(finalHigh), balls.get(j));
					}
				}
			});
			low++;
			high--;
		}
 */

/*
var allBalls = board.getAllBalls();
		var workAmount = allBalls.size() / nWorker;
		var indexLeft = 0;
		var indexRight = workAmount - 1;
		for (int nw = 0; nw < nWorker; nw++) {
			int finalIndexLeft = indexLeft;
			int finalIndexRight = indexRight;
			workBuffer.put(() -> {
				for (int i = finalIndexLeft; i < finalIndexRight; i++) {
					for (int j = i + 1; j < allBalls.size(); j++) {
						Ball.resolveCollision(allBalls.get(i), allBalls.get(j));
					}
				}
			});
			indexLeft += workAmount;
			indexRight += workAmount;
		}
 */
