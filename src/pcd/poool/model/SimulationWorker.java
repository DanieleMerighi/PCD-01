package pcd.poool.model;

import pcd.poool.util.WorkBuffer;

public class SimulationWorker extends Thread {

	private final Board board;
	private final WorkBuffer workBuffer;

	public SimulationWorker(Board board, WorkBuffer workBuffer) {
		this.board = board;
		this.workBuffer = workBuffer;
	}

	@Override
	public void run() {
		log("started.");
		while (!board.isGameOver()) {
			var work = workBuffer.get();
			work.run();
			workBuffer.done();
		}
	}

	private void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + "][ Worker-" + getName() + " ] " + msg);
	}
}
