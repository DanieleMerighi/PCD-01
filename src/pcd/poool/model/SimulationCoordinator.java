package pcd.poool.model;

import pcd.poool.util.*;

import java.util.ArrayList;
import java.util.List;

public class SimulationCoordinator extends Thread {

	private final Board board;
	private final List<BoardObserver> observers;
	private final WorkBuffer workBuffer;
	
	public SimulationCoordinator(
			Board board,
			List<BoardObserver> observers,
			WorkBuffer workBuffer
	) {
		this.board = board;
		this.observers = new ArrayList<>(observers);
		this.workBuffer = workBuffer;
	}

	@Override
	public void run() {
		long nFrames = 0;
		long t0 = System.currentTimeMillis();
		long lastUpdateTime = System.currentTimeMillis();
		while (!board.isGameOver()) {
			long elapsed = System.currentTimeMillis() - lastUpdateTime;
			lastUpdateTime = System.currentTimeMillis();
			board.updateState(elapsed, workBuffer);

			nFrames++;
			long framePerSec = 0;
			long dt = (System.currentTimeMillis() - t0);
			if (dt > 0) {
				framePerSec = nFrames*1000/dt;
			}
			notifyObservers(framePerSec);
		}
		for (var o : observers) {
			o.gameOver(board.getGameResult());
		}
	}

	private void notifyObservers(long framePerSec) {
		for (var o: observers) {
			o.modelUpdated(board.getBoardViewInfo(), framePerSec);
		}
	}
}
