package pcd.poool.model;

import java.util.Random;

public class BotUpdater extends Thread {

	private final Board board;

	public BotUpdater(Board board) {
		this.board = board;
	}

	@Override
	public void run() {
		var random = new Random(System.currentTimeMillis());
		while (!board.isGameOver()) {
			try {
				Thread.sleep(random.nextLong(400, 1000));
				board.kickBotBall();
			}
			catch (InterruptedException ignored) {
			}
		}
	}
}
