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
 * Scenario: HUMAN dentro la buca sinistra E BOT dentro la buca destra,
 * entrambe gia' al tick 1.
 *
 * Configurazione delle ball: HUMAN, BOT, 1 small ball (totale 3 ball,
 * dispari di proposito). distributeLinearWork con nActualWorker=2 fa
 * workAmount = 3/2 = 1, quindi:
 *   Worker 0 -> indice 0 = HUMAN
 *   Worker 1 -> indici 1,2 = BOT + small
 * I due worker eseguono resolveHole in parallelo: entrambi chiamano
 * gameState.endGame(...) nello stesso tick, sul thread proprio,
 * mentre il coordinator e' bloccato su workLatch.await().
 *
 * Verifica:
 * - la mutua esclusione su GameState.endGame (e' synchronized) regge
 *   sotto contesa concorrente,
 * - gameResult finisce o "Bot wins! Human fell in a hole." o
 *   "Human wins! Bot fell in a hole." a seconda di chi vince la corsa,
 *   mai uno stato corrotto,
 * - in entrambi i casi il programma termina pulito (no deadlock, no race).
 *
 * JPF dovrebbe esplorare almeno 2 stati di terminazione distinti (uno
 * per ciascun ordine di esecuzione delle due endGame).
 */
public class HumanAndBotFallSameTickJpf {

    private static final int N_WORKERS = 2;
    private static final int MAX_TICKS = 2;

    public static void main(String[] argv) {

        BoardConf boardConf = new BothInHolesConf();
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
     * HUMAN in (-1.45, 0.95): distanza ~0.07 dal centro buca sinistra (raggio 0.2) => in buca.
     * BOT in (1.45, 0.95): distanza ~0.07 dal centro buca destra => in buca.
     * 1 small ball al centro, ferma, lontana da tutto: serve solo a rendere
     * il totale di ball dispari (3) cosi' che distributeLinearWork separi
     * HUMAN e BOT su due worker distinti.
     */
    private static class BothInHolesConf implements BoardConf {

        @Override
        public Ball getHumanBall() {
            return new Ball(new P2d(-1.45, 0.95), 0.06, 1, new V2d(0, 0), BallType.HUMAN);
        }

        @Override
        public Ball getBotBall() {
            return new Ball(new P2d(1.45, 0.95), 0.06, 1, new V2d(0, 0), BallType.BOT);
        }

        @Override
        public List<Ball> getSmallBalls() {
            List<Ball> balls = new ArrayList<Ball>();
            balls.add(new Ball(new P2d(0, 0), 0.05, 0.5, new V2d(0, 0)));
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
