package pcd.pooolThreadOrientedJpf.scenarios;

import pcd.pooolThreadOrientedJpf.model.Ball;
import pcd.pooolThreadOrientedJpf.model.BallType;
import pcd.pooolThreadOrientedJpf.model.Board;
import pcd.pooolThreadOrientedJpf.model.BoardConf;
import pcd.pooolThreadOrientedJpf.model.Boundary;
import pcd.pooolThreadOrientedJpf.model.Hole;
import pcd.pooolThreadOrientedJpf.model.P2d;
import pcd.pooolThreadOrientedJpf.model.SimulationCoordinator;
import pcd.pooolThreadOrientedJpf.model.SimulationWorker;
import pcd.pooolThreadOrientedJpf.model.V2d;
import pcd.pooolThreadOrientedJpf.util.LatchImpl;
import pcd.pooolThreadOrientedJpf.util.SynchBox;
import pcd.pooolThreadOrientedJpf.util.SynchBoxImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Scenario: tutte le small ball gia' dentro le buche al tick 1.
 * Nessun worker chiama endGame (le small ball fanno solo removeSmallBall).
 * Dopo la fase resolveHole smallBalls.isEmpty() e' true, quindi e' il
 * COORDINATOR a chiamare setEndGame(). Verifica lo shutdown nel ramo
 * "coordinator-driven termination".
 */
public class AllSmallBallsFallTogetherJpf {

    private static final int N_WORKERS = 2;
    private static final int MAX_TICKS = 2;

    public static void main(String[] argv) {

        BoardConf boardConf = new AllSmallInHolesConf();
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

    /**
     * HUMAN e BOT al sicuro al centro, entrambe le small ball gia' dentro
     * le due buche (distanza ~0.07 dal centro buca, raggio buca 0.2).
     */
    private static class AllSmallInHolesConf implements BoardConf {

        @Override
        public Ball getHumanBall() {
            return new Ball(new P2d(-0.6, 0), 0.06, 1, new V2d(0, 0), BallType.HUMAN);
        }

        @Override
        public Ball getBotBall() {
            return new Ball(new P2d(0.6, 0), 0.06, 1, new V2d(0, 0), BallType.BOT);
        }

        @Override
        public List<Ball> getSmallBalls() {
            List<Ball> balls = new ArrayList<Ball>();
            balls.add(new Ball(new P2d(-1.45, 0.95), 0.05, 0.75, new V2d(0, 0)));
            balls.add(new Ball(new P2d(1.45, 0.95), 0.025, 0.25, new V2d(0, 0)));
            return balls;
        }

        @Override
        public Boundary getBoardBoundary() {
            return new Boundary(-1.5, -1.0, 1.5, 1.0);
        }

        @Override
        public List<Hole> getHoles() {
            double radius = 0.2;
            return List.of(
                    new Hole(new P2d(-1.5, 1.0), radius),
                    new Hole(new P2d(1.5, 1.0), radius));
        }
    }
}
