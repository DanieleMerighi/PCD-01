package pcd.pooolThreadOrientedJpf.model;

import pcd.pooolThreadOrientedJpf.util.Latch;
import pcd.pooolThreadOrientedJpf.util.SynchBox;

import java.util.List;
import java.util.function.Consumer;

public class SimulationCoordinator extends Thread {

    // In JPF System.currentTimeMillis() non avanza in modo realistico
    // tra una transizione e l'altra, quindi l'elapsed misurato sarebbe ~0
    // e le palle non si muoverebbero mai. Uso un dt fisso al posto del
    // tempo trascorso,
    private static final long FIXED_TICK_DT_MS = 16;

    private final Board board;
    private final GameState gameState;
    private final List<SynchBox<Runnable>> workBuffer;
    private final Latch workLatch;
    private final int maxTicks;

    public SimulationCoordinator(
            Board board,
            List<SynchBox<Runnable>> workBuffer,
            Latch workLatch,
            int maxTicks
    ) {
        this.board = board;
        this.gameState = board.getState();
        this.workBuffer = workBuffer;
        this.workLatch = workLatch;
        this.maxTicks = maxTicks;
    }

    @Override
    public void run() {
        int tick = 0;
        while (!gameState.isGameOver() && tick < maxTicks) {
            this.updateState(FIXED_TICK_DT_MS);
            tick++;
        }
        for (SynchBox<Runnable> box : workBuffer) {
            box.end();
        }
    }

    private void updateState(long dt) {
        distributeLinearWork(new Consumer<Ball>() {
            @Override
            public void accept(Ball ball) {
                ball.updateState(dt, board);
            }
        });

        distributeLinearWork(new Consumer<Ball>() {
            @Override
            public void accept(Ball ball) {
                for (Hole hole : board.getHoles()) {
                    Ball.resolveHole(ball, hole, gameState);
                }
            }
        });

        if (gameState.isGameOver()) {
            return;
        }

        List<Ball> smallBalls = gameState.getSmallBalls();
        if (smallBalls.isEmpty()) {
            setEndGame();
            return;
        }

        final List<Ball> allBalls = gameState.getAllBalls();
        final int nActualWorker = Math.min(workBuffer.size(), allBalls.size());

        distributeWork(new Consumer<Integer>() {
            @Override
            public void accept(Integer workerIndex) {
                int idx = workerIndex.intValue();
                for (int i = idx; i < allBalls.size() - 1; i += nActualWorker) {
                    for (int j = i + 1; j < allBalls.size(); j++) {
                        Ball.resolveCollision(allBalls.get(i), allBalls.get(j));
                    }
                }
            }
        }, nActualWorker);
    }

    public void distributeWork(final Consumer<Integer> action, int nActualWorker) {
        workLatch.reset(nActualWorker);
        for (int i = 0; i < nActualWorker; i++) {
            final int workerIndex = i;
            workBuffer.get(i).put(new Runnable() {
                @Override
                public void run() {
                    action.accept(Integer.valueOf(workerIndex));
                }
            });
        }
        workLatch.await();
    }

    public void distributeLinearWork(final Consumer<Ball> action) {
        final List<Ball> allBalls = gameState.getAllBalls();
        final int totalSize = allBalls.size();

        final int nActualWorker = Math.min(workBuffer.size(), totalSize);
        final int workAmount = totalSize / nActualWorker;

        distributeWork(new Consumer<Integer>() {
            @Override
            public void accept(Integer workerIndex) {
                int idx = workerIndex.intValue();
                int start = idx * workAmount;
                int end = (idx == nActualWorker - 1) ? totalSize : start + workAmount;
                for (int j = start; j < end; j++) {
                    action.accept(allBalls.get(j));
                }
            }
        }, nActualWorker);
    }

    private void setEndGame() {
        int humanScore = gameState.getHumanScore();
        int botScore = gameState.getBotScore();
        String gameResult;
        if (humanScore > botScore) {
            gameResult = "Human wins! " + humanScore + " - " + botScore;
        } else if (botScore > humanScore) {
            gameResult = "Bot wins! " + botScore + " - " + humanScore;
        } else {
            gameResult = "Draw! " + humanScore + " - " + botScore;
        }
        gameState.endGame(gameResult);
    }
}
