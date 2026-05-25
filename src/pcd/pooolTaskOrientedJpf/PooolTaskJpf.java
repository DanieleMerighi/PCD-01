package pcd.pooolTaskOrientedJpf;

import pcd.pooolTaskOrientedJpf.model.Board;
import pcd.pooolTaskOrientedJpf.model.MinimalBoardConf;
import pcd.pooolTaskOrientedJpf.model.SimulationCoordinator;

public class PooolTaskJpf {

    private static final int N_WORKERS = 2;
    private static final int MAX_TICKS = 2;

    public static void main(String[] argv) {
        MinimalBoardConf boardConf = new MinimalBoardConf();
        Board board = new Board(boardConf);

        SimulationCoordinator coordinator = new SimulationCoordinator(
                board, N_WORKERS, MAX_TICKS);
        coordinator.start();
    }
}
