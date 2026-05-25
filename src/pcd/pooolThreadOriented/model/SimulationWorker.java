package pcd.pooolThreadOriented.model;

import pcd.pooolThreadOriented.util.Latch;
import pcd.pooolThreadOriented.util.SynchBox;

import java.util.Optional;

public class SimulationWorker extends Thread {

	private final SynchBox<Runnable> workBox;
	private final Latch workLatch;

	public SimulationWorker(SynchBox<Runnable> workBox, Latch workLatch) {
		this.workBox = workBox;
		this.workLatch = workLatch;
	}

	@Override
	public void run() {
		while (true) {
			Optional<Runnable> work = workBox.get();
			if (work.isEmpty()) {
				break;
			}
			work.get().run();
			workLatch.countDown();
		}
	}

}
