package pcd.pooolTaskOriented.model;

import java.util.Random;

public class BotUpdater extends Thread {

	private final Board board;
	private final GameState gameState;

	public BotUpdater(Board board) {
		this.board = board;
		this.gameState = board.getState();
	}

	@Override
	public void run() {
		var random = new Random(System.currentTimeMillis());
		while (!gameState.isGameOver()) {
			try {
				Thread.sleep(random.nextLong(400, 1000));
				board.kickBotBall();
			} catch (InterruptedException ignored) {}
		}
	}

}
