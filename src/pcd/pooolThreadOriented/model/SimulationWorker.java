package pcd.pooolThreadOriented.model;

import pcd.pooolThreadOriented.util.Latch;
import pcd.pooolThreadOriented.util.SynchCell;

import java.util.Optional;

public class SimulationWorker extends Thread {

	private final SynchCell<Runnable> workCell;
	private final Latch workLatch;

	public SimulationWorker(SynchCell<Runnable> workCell, Latch workLatch) {
		this.workCell = workCell;
		this.workLatch = workLatch;
	}

	@Override
	public void run() {
		while (true) {
			Optional<Runnable> work = workCell.get();
			if (work.isEmpty()) {
				break;
			}
			work.get().run();
			workLatch.countDown();
		}
	}

}
