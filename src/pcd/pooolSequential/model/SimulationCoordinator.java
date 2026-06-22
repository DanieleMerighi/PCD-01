package pcd.pooolSequential.model;

import java.util.ArrayList;
import java.util.List;

public class SimulationCoordinator extends Thread {

	private final Board board;
	private final GameState gameState;
	private final List<BoardObserver> observers;
	private final SpatialGrid grid;
	
	public SimulationCoordinator(Board board, List<BoardObserver> observers) {
		this.board = board;
		this.gameState = board.getState();
		this.observers = new ArrayList<>(observers);
		double maxSmallRadius = 0.0;
		for (Ball b : board.getSmallBalls()) {
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
	}

	private void updateState(long dt) {
		var allBalls = board.getAllBalls();

		for (var b : allBalls) {
			b.updateState(dt, board);
		}

		for (var ball : allBalls) {
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

		grid.clearAndPopulate(board.getSmallBalls(), board.getBounds());

		for (int r = 0; r < grid.getRows(); r++) {
			for (int c = 0; c < grid.getCols(); c++) {
				var cellBalls = grid.getCell(c, r);
				if (cellBalls.isEmpty()) continue;

				var nearbyBalls = grid.getNearbyBalls(c, r);

				for (Ball b1 : cellBalls) {
					for (Ball b2 : nearbyBalls) {
						if (b1.getId() < b2.getId()) {
							Ball.resolveCollision(b1, b2);
						}
					}
				}
			}
		}
		var mainBalls = board.getMainBalls();
		allBalls = board.getAllBalls();
		for (Ball mainBall : mainBalls) {
			for (Ball otherBall : allBalls) {
				if (mainBall.getId() != otherBall.getId()) {
					Ball.resolveCollision(mainBall, otherBall);
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
