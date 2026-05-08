package pcd.pooolThreadOriented.model;

import pcd.pooolThreadOriented.util.Latch;
import pcd.pooolThreadOriented.util.SynchBox;

public class SimulationWorker extends Thread {

	private final SynchBox<Runnable> workBox;
	private final Latch workLatch;
	private final GameState gameState;

	public SimulationWorker(SynchBox<Runnable> workBox, Latch workLatch, GameState gameState) {
		this.workBox = workBox;
		this.workLatch = workLatch;
		this.gameState = gameState;
	}

	@Override
	public void run() {
		while (!gameState.isGameOver()) {
			var work = workBox.get();
			work.run();
			workLatch.countDown();
		}
	}

}
