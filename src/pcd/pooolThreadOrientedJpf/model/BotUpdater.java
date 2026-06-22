package pcd.pooolThreadOrientedJpf.model;

public class BotUpdater extends Thread {

	private final Board board;

	public BotUpdater(Board board) {
		this.board = board;
	}

	@Override
	public void run() {
		board.kickBotBall();
	}

}
