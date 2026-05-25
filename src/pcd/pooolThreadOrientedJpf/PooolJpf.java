package pcd.pooolThreadOrientedJpf;

import pcd.pooolThreadOrientedJpf.model.Board;
import pcd.pooolThreadOrientedJpf.model.MinimalBoardConf;
import pcd.pooolThreadOrientedJpf.model.SimulationCoordinator;
import pcd.pooolThreadOrientedJpf.model.SimulationWorker;
import pcd.pooolThreadOrientedJpf.util.LatchImpl;
import pcd.pooolThreadOrientedJpf.util.SynchBox;
import pcd.pooolThreadOrientedJpf.util.SynchBoxImpl;

import java.util.ArrayList;
import java.util.List;

public class PooolJpf {

    private static final int N_WORKERS = 2;
    private static final int MAX_TICKS = 2;

    public static void main(String[] argv) {

        MinimalBoardConf boardConf = new MinimalBoardConf();
        Board board = new Board(boardConf);

        List<SynchBox<Runnable>> workBuffer = new ArrayList<SynchBox<Runnable>>(N_WORKERS);
        LatchImpl workLatch = new LatchImpl(N_WORKERS);

        for (int i = 0; i < N_WORKERS; i++) {
            SynchBox<Runnable> workBox = new SynchBoxImpl<Runnable>();
            workBuffer.add(workBox);
            SimulationWorker worker = new SimulationWorker(workBox, workLatch, board.getState());
            worker.start();
        }

        SimulationCoordinator coordinator = new SimulationCoordinator(
                board, workBuffer, workLatch, MAX_TICKS);
        coordinator.start();
    }
}
