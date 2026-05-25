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
import pcd.pooolThreadOrientedJpf.util.SynchCell;
import pcd.pooolThreadOrientedJpf.util.SynchCellImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Scenario: due small ball posizionate in modo che dist == r1+r2 esatto.
 * La condizione in resolveCollision e' "dist < minD && dist > 1e-6",
 * quindi NON dovrebbe scattare la collisione. Verifica il bordo della
 * condizione sotto tutti gli interleaving (entrambi i worker leggono e
 * scrivono i campi pos/vel delle stesse due ball via setter sincronizzati).
 */
public class BoundaryCollisionJpf {

    private static final int N_WORKERS = 2;
    private static final int MAX_TICKS = 2;

    public static void main(String[] argv) {

        BoardConf boardConf = new TouchingBallsConf();
        Board board = new Board(boardConf);

        List<SynchCell<Runnable>> workBuffer = new ArrayList<SynchCell<Runnable>>(N_WORKERS);
        LatchImpl workLatch = new LatchImpl(N_WORKERS);

        for (int i = 0; i < N_WORKERS; i++) {
            SynchCell<Runnable> workCell = new SynchCellImpl<Runnable>();
            workBuffer.add(workCell);
            SimulationWorker worker = new SimulationWorker(workCell, workLatch);
            worker.start();
        }

        SimulationCoordinator coordinator = new SimulationCoordinator(
                board, workBuffer, workLatch, MAX_TICKS);
        coordinator.start();
    }

    /**
     * small ball A in (0, 0) r=0.05, small ball B in (0.075, 0) r=0.025
     * => distanza tra i centri = 0.075, somma raggi = 0.075 => si toccano.
     */
    private static class TouchingBallsConf implements BoardConf {

        @Override
        public Ball getHumanBall() {
            return new Ball(new P2d(-0.8, 0), 0.06, 1, new V2d(0, 0), BallType.HUMAN);
        }

        @Override
        public Ball getBotBall() {
            return new Ball(new P2d(0.8, 0), 0.06, 1, new V2d(0, 0), BallType.BOT);
        }

        @Override
        public List<Ball> getSmallBalls() {
            List<Ball> balls = new ArrayList<Ball>();
            balls.add(new Ball(new P2d(0, 0), 0.05, 0.75, new V2d(0, 0)));
            balls.add(new Ball(new P2d(0.075, 0), 0.025, 0.25, new V2d(0, 0)));
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
