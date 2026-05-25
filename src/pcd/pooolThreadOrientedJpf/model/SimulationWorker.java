package pcd.pooolThreadOrientedJpf.model;

import pcd.pooolThreadOrientedJpf.util.Latch;
import pcd.pooolThreadOrientedJpf.util.SynchBox;

import java.util.Optional;

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
