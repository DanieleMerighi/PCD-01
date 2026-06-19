package pcd.pooolTaskOrientedJpf.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class SimulationCoordinator extends Thread {

    private static final long FIXED_TICK_DT_MS = 16;

    private final Board board;
    private final GameState gameState;
    private final ExecutorService exec;
    private final int nWorker;
    private final int maxTicks;

    public SimulationCoordinator(Board board, int nWorker, int maxTicks) {
        this.board = board;
        this.gameState = board.getState();
        this.exec = Executors.newFixedThreadPool(nWorker);
        this.nWorker = nWorker;
        this.maxTicks = maxTicks;
    }

    @Override
    public void run() {
        int tick = 0;
        while (!gameState.isGameOver() && tick < maxTicks) {
            this.updateState(FIXED_TICK_DT_MS);
            tick++;
        }
    }

    private void updateState(final long dt) {
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

        if (gameState.isSmallBallEmpty()) {
            setEndGame();
            return;
        }

        final List<Ball> allBalls = gameState.getAllBalls();
        final int nActualWorker = Math.min(nWorker, allBalls.size());

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
        List<Callable<Void>> work = new ArrayList<Callable<Void>>();
        for (int i = 0; i < nActualWorker; i++) {
            final int workerIndex = i;
            work.add(new Callable<Void>() {
                @Override
                public Void call() {
                    action.accept(Integer.valueOf(workerIndex));
                    return null;
                }
            });
        }
        try {
            exec.invokeAll(work);
        } catch (Exception ignored) {
        }
    }

    public void distributeLinearWork(final Consumer<Ball> action) {
        final List<Ball> allBalls = gameState.getAllBalls();
        final int totalSize = allBalls.size();

        final int actualWorkers = Math.min(nWorker, totalSize);
        final int workAmount = totalSize / actualWorkers;

        distributeWork(new Consumer<Integer>() {
            @Override
            public void accept(Integer workerIndex) {
                int idx = workerIndex.intValue();
                int start = idx * workAmount;
                int end = (idx == actualWorkers - 1) ? totalSize : start + workAmount;
                for (int j = start; j < end; j++) {
                    action.accept(allBalls.get(j));
                }
            }
        }, actualWorkers);
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
