package pcd.poool.model;

import java.util.ArrayList;
import java.util.List;

public class AutonomousUpdater extends Thread {

	private final Board board;
	private final List<BoardObserver> observers;
	
	public AutonomousUpdater(Board board, List<BoardObserver> observers) {
		this.board = board;
		this.observers = new ArrayList<>(observers);
	}

	private static final long TARGET_FRAME_MS = 16; // ~60fps

	@Override
	public void run() {
		int nFrames = 0;
		long t0 = System.currentTimeMillis();
		long lastUpdateTime = System.currentTimeMillis();
		while (!board.isGameOver()) {
			long elapsed = System.currentTimeMillis() - lastUpdateTime;
			lastUpdateTime = System.currentTimeMillis();
			board.updateState(elapsed);

			nFrames++;
			int framePerSec = 0;
			long dt = (System.currentTimeMillis() - t0);
			if (dt > 0) {
				framePerSec = (int)(nFrames*1000/dt);
			}
			notifyObservers(framePerSec);

			long frameTime = System.currentTimeMillis() - lastUpdateTime;
			long sleepTime = TARGET_FRAME_MS - frameTime;
			if (sleepTime > 0) {
				try { Thread.sleep(sleepTime); } catch (InterruptedException ignored) {}
			}
		}
		for (var o : observers) {
			o.gameOver(board.getGameResult());
		}
	}

	private void notifyObservers(int framePerSec) {
		for (var o: observers) {
			o.modelUpdated(board.getBoardViewInfo(), framePerSec);
		}
	}
}
