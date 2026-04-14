package pcd.model;

import java.util.ArrayList;
import java.util.List;

public class AutonomousUpdater extends Thread {

	private Board board;
	private List<BoardObserver> observers;
	
	public AutonomousUpdater(Board board) {
		this.board = board;
		this.observers = new ArrayList<>();
	}

	public void run() {
		int nFrames = 0;
		long t0 = System.currentTimeMillis();
		long lastUpdateTime = System.currentTimeMillis();
		while (true) {
			long elapsed = System.currentTimeMillis() - lastUpdateTime;
			lastUpdateTime = System.currentTimeMillis();
			board.updateState(elapsed);

			/* render */

			nFrames++;
			int framePerSec = 0;
			long dt = (System.currentTimeMillis() - t0);
			if (dt > 0) {
				framePerSec = (int)(nFrames*1000/dt);
			}
			notifyObservers(framePerSec);
		}
	}

	public void addObserver(BoardObserver o) {
		observers.add(o);
	}

	private void notifyObservers(int framePerSec) {
		for (var o: observers) {
			o.modelUpdated(board, framePerSec);
		}
	}
}
