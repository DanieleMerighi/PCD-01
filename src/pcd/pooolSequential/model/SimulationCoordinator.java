package pcd.pooolSequential.model;

import java.util.ArrayList;
import java.util.List;

public class SimulationCoordinator extends Thread {

	private final Board board;
	private final GameState gameState;
	private final List<BoardObserver> observers;
	
	public SimulationCoordinator(Board board, List<BoardObserver> observers) {
		this.board = board;
		this.gameState = board.getState();
		this.observers = new ArrayList<>(observers);
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
	}

	private void updateState(long dt) {
		var allBalls = gameState.getAllBalls();

		for (var b : allBalls) {
			b.updateState(dt, board);
		}

		for (var ball : allBalls) {
			for (var hole : board.getHoles()) {
				Ball.resolveHole(ball, hole, gameState);
			}
		}

		if (gameState.isGameOver())
			return;

		if (gameState.isSmallBallEmpty()) {
			setEndGame();
			return;
		}

		allBalls = gameState.getAllBalls();
		for (int i = 0; i < allBalls.size() - 1; i++) {
			for (int j = i + 1; j < allBalls.size(); j++) {
				Ball.resolveCollision(allBalls.get(i), allBalls.get(j));
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
