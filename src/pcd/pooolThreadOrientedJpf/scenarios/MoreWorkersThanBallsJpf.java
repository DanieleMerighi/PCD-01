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
import java.util.Collections;
import java.util.List;

/**
 * Scenario: N_WORKERS = 3, BoardConf con solo HUMAN + BOT (zero small ball).
 * "nActualWorker = min(workBuffer.size(), allBalls.size())" vale min(3, 2) = 2:
 * Worker 0 e Worker 1 ricevono task, Worker 2 NON riceve mai nulla per tutta
 * la simulazione e resta bloccato in workCell.get() dall'istante zero.
 *
 * Inoltre con 0 small ball il primo tick entra subito nel ramo
 * "if (smallBalls.isEmpty()) setEndGame()" del coordinator, quindi la
 * simulazione termina molto in fretta e lo state space resta gestibile
 * (la versione precedente con N_WORKERS=5 dava OOM dopo 7h30).
 *
 * Verifica che lo shutdown via SynchCell.end() risvegli e termini
 * correttamente anche il worker idle che non si e' mai svegliato dal
 * suo primo wait().
 */
public class MoreWorkersThanBallsJpf {

    private static final int N_WORKERS = 3;
    private static final int MAX_TICKS = 2;

    public static void main(String[] argv) {

        BoardConf boardConf = new TwoBallsConf();
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
     * Solo HUMAN e BOT, ferme e lontane dalle buche. Nessuna small ball.
     */
    private static class TwoBallsConf implements BoardConf {

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
            return Collections.emptyList();
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
