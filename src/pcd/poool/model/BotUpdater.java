package pcd.poool.model;

import java.util.Random;

public class BotUpdater extends Thread {

	private final Board board;
	private final GameState gameState;

	public BotUpdater(Board board, GameState gameState) {
		this.board = board;
		this.gameState = gameState;
	}

	@Override
	public void run() {
		var random = new Random(System.currentTimeMillis());
		while (!gameState.isGameOver()) {
			try {
				Thread.sleep(random.nextLong(400, 1000));
				board.kickBotBall();
			}
			catch (InterruptedException ignored) {
			}
		}
	}
}
