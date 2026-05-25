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
 * Scenario: due small ball gia' sovrapposte all'inizio e in rotta di
 * collisione (vel opposte sull'asse x). La condizione "dist < minD"
 * scatta subito al tick 1 e Ball.resolveCollision viene eseguita per
 * davvero (non solo la fase di check come in BoundaryCollisionJpf):
 * setPos/setVel su entrambe le ball, vel update via dvn < 0.
 *
 * Mentre Worker 0 processa la coppia (small1, small2) e ne modifica
 * pos e vel, Worker 1 sta processando le coppie (BOT, small1) e
 * (BOT, small2): leggono pos e radius delle stesse small ball via
 * i getter sincronizzati.
 */
public class ActualCollisionJpf {

    private static final int N_WORKERS = 2;
    private static final int MAX_TICKS = 2;

    public static void main(String[] argv) {

        BoardConf boardConf = new CollidingPairConf();
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
     * small1 in (-0.04, 0) con vel (1, 0), small2 in (0.04, 0) con vel (-1, 0).
     * Raggi entrambi 0.05 => somma raggi 0.10. Distanza iniziale 0.08 < 0.10
     * quindi gia' compenetrate al tick 1 e con velocita' relativa che le
     * spinge l'una contro l'altra (dvn = -2 < 0): scatta tutto il blocco
     * di resolveCollision (correzione di overlap + impulso sulle velocita').
     *
     * HUMAN e BOT lontane e ferme, non interferiscono.
     */
    private static class CollidingPairConf implements BoardConf {

        @Override
        public Ball getHumanBall() {
            return new Ball(new P2d(-0.6, -0.5), 0.06, 1, new V2d(0, 0), BallType.HUMAN);
        }

        @Override
        public Ball getBotBall() {
            return new Ball(new P2d(0.6, -0.5), 0.06, 1, new V2d(0, 0), BallType.BOT);
        }

        @Override
        public List<Ball> getSmallBalls() {
            List<Ball> balls = new ArrayList<Ball>();
            balls.add(new Ball(new P2d(-0.04, 0), 0.05, 0.5, new V2d(1, 0)));
            balls.add(new Ball(new P2d(0.04, 0), 0.05, 0.5, new V2d(-1, 0)));
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
