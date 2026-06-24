package pcd.pooolThreadOrientedJpf.model;

import pcd.pooolThreadOrientedJpf.util.Latch;
import pcd.pooolThreadOrientedJpf.util.SynchCell;

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
    private final List<SynchCell<Runnable>> workBuffer;
    private final Latch workLatch;
    private final int maxTicks;
    private final SpatialGrid grid;

    public SimulationCoordinator(
            Board board,
            List<SynchCell<Runnable>> workBuffer,
            Latch workLatch,
            int maxTicks
    ) {
        this.board = board;
        this.gameState = board.getState();
        this.workBuffer = workBuffer;
        this.workLatch = workLatch;
        this.maxTicks = maxTicks;
        this.grid = new SpatialGrid(board.getBounds(), 0.9);
    }

    @Override
    public void run() {
        int tick = 0;
        while (!gameState.isGameOver() && tick < maxTicks) {
            this.updateState(FIXED_TICK_DT_MS);
            tick++;
        }
        for (SynchCell<Runnable> cell : workBuffer) {
            cell.end();
        }
    }

    private void updateState(long dt) {
        distributeLinearWork(new Consumer<Ball>() {
            @Override
            public void accept(Ball ball) {
                ball.updateState(dt, board);
                for (Hole hole : board.getHoles()) {
                    Ball.resolveHole(ball, hole, board, gameState);
                }
            }
        });

        if (gameState.isGameOver()) {
            return;
        }

        if (board.isSmallBallEmpty()) {
            setEndGame();
            return;
        }

        grid.clearAndPopulate(board.getAllBalls(), board.getBounds());

        final int totalRows = grid.getRows();
        final int nActualWorker = Math.min(workBuffer.size(), totalRows);

        // ---------------------------------------------------------
        // PASSATA 1: Collisioni Righe PARI (0, 2, 4, 6...)
        // ---------------------------------------------------------
        distributeWork(new Consumer<Integer>() {
            @Override
            public void accept(Integer workerIndex) {
                for (int r = 0; r < totalRows; r += 2) {
                    // Interleavizzazione logica delle sole righe pari tra i worker
                    if ((r / 2) % nActualWorker == workerIndex) {
                        processRowCollisions(r);
                    }
                }
            }
        }, nActualWorker);

        // ---------------------------------------------------------
        // PASSATA 2: Collisioni Righe DISPARI (1, 3, 5, 7...)
        // ---------------------------------------------------------
        distributeWork(new Consumer<Integer>() {
            @Override
            public void accept(Integer workerIndex) {
                for (int r = 1; r < totalRows; r += 2) {
                    // Interleavizzazione logica delle sole righe dispari tra i worker
                    if ((r / 2) % nActualWorker == workerIndex) {
                        processRowCollisions(r);
                    }
                }
            }
        }, nActualWorker);
    }

    // Metodo di supporto per calcolare le collisioni intra e inter cella di una riga
	private void processRowCollisions(int r) {
		final int totalCols = grid.getCols();
		for (int c = 0; c < totalCols; c++) {
			List<Ball> cellBalls = grid.getCell(c, r);
			if (cellBalls.isEmpty()) continue;

			// 1. Collisioni INTRA-cella
			int cSize = cellBalls.size();
			for (int i = 0; i < cSize; i++) {
				Ball b1 = cellBalls.get(i);
				for (int j = i + 1; j < cSize; j++) {
					Ball.resolveCollision(b1, cellBalls.get(j));
				}
			}

			// 2. Collisioni INTER-cella (con i 4 vicini in avanti/basso)
			List<Ball> nearbyBalls = grid.getForwardNeighbors(c, r);
			for (int i = 0; i < cSize; i++) {
				Ball b1 = cellBalls.get(i);
				for (var b2 : nearbyBalls) {
					Ball.resolveCollision(b1, b2);
				}
			}
		}
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
        final List<Ball> allBalls = board.getAllBalls();
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
