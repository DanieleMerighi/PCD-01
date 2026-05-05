package pcd.poool.model;

import pcd.poool.util.WorkBuffer;

public class SimulationWorker extends Thread {

	private final GameState gameState;
	private final WorkBuffer workBuffer;

	public SimulationWorker(GameState gameState, WorkBuffer workBuffer) {
		this.gameState = gameState;
		this.workBuffer = workBuffer;
	}

	@Override
	public void run() {
		log("started.");
		while (!gameState.isGameOver()) {
			var work = workBuffer.get();
			work.run();
			workBuffer.done();
		}
		workBuffer.clearAll();
	}

	private void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + "][ Worker-" + getName() + " ] " + msg);
	}
}
