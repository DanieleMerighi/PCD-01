package pcd.pooolThreadOriented.model;

import pcd.pooolThreadOriented.util.Latch;

public class SimulationWorker extends Thread {

	private final Latch latch;
	private final Runnable work;

	public SimulationWorker(Latch latch, Runnable work) {
		this.latch = latch;
		this.work = work;
	}

	@Override
	public void run() {
		log("started.");
		work.run();
		latch.countDown();
	}

	private void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + "][ Worker-" + getName() + " ] " + msg);
	}

}
