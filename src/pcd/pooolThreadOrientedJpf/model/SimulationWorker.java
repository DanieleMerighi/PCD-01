package pcd.pooolThreadOrientedJpf.model;

import pcd.pooolThreadOrientedJpf.util.Latch;
import pcd.pooolThreadOrientedJpf.util.SynchBox;

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
            Runnable work = workBox.get();
            work.run();
            workLatch.countDown();
        }
    }
}
