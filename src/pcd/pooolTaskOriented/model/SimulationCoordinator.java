package pcd.pooolTaskOriented.model;

import pcd.pooolTaskOriented.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class SimulationCoordinator extends Thread {

	private final Board board;
	private final GameState gameState;
	private final List<BoardObserver> observers;
	private final ExecutorService exec;
	private final int nTasks;
	private final SpatialGrid grid;

	public SimulationCoordinator(
			Board board,
			List<BoardObserver> observers,
            int nWorker,
			int nTasks
	) {
		this.board = board;
		this.gameState = board.getState();
		this.observers = List.copyOf(observers);
		this.exec = Executors.newFixedThreadPool(nWorker);
		this.nTasks = nTasks;
		double maxSmallRadius = 0.0;
		for (Ball b : board.getAllBalls()) {
			if (b.getRadius() > maxSmallRadius) {
				maxSmallRadius = b.getRadius();
			}
		}
		this.grid = new SpatialGrid(board.getBounds(), maxSmallRadius);
	}

	@Override
	public void run() {
		long nTicks = 0;
		long t0 = System.currentTimeMillis();
		long lastUpdateTime = System.currentTimeMillis();
		long tickPerSec = 0;
		while (!gameState.isGameOver()) {
			long elapsed = System.currentTimeMillis() - lastUpdateTime;
			lastUpdateTime = System.currentTimeMillis();

			this.updateState(elapsed);

			nTicks++;
			tickPerSec = 0;
			long dt = (System.currentTimeMillis() - t0);
			if (dt > 0) {
				tickPerSec = nTicks*1000/dt;
			}
			notifyObservers(tickPerSec);
		}
		for (var o : observers) {
			o.gameOver(board.getBoardViewInfo(), gameState.getGameStateViewInfo(), tickPerSec, gameState.getGameResult());
		}
		exec.shutdown();
	}

	private void updateState(long dt) {
		board.applyHumanKick();
		board.applyBotKick();

        distributeLinearTasks(board.getAllBalls(), ball -> {
            ball.updateState(dt, board);
            for (var hole : board.getHoles()) {
                Ball.resolveHole(ball, hole, board, gameState);
            }
        });

		if (gameState.isGameOver())
			return;

		if (board.isSmallBallEmpty()) {
			setEndGame();
			return;
		}

		grid.clearAndPopulate(board.getAllBalls(), board.getBounds());

		final int totalRows = grid.getRows();

		// ---------------------------------------------------------
		// PASSATA 1: Collisioni Righe PARI (0, 2, 4, 6...)
		// ---------------------------------------------------------
        runTasks(
                IntRange.withStep(0, totalRows, 2),
                this::processRowCollisions
        );

		// ---------------------------------------------------------
		// PASSATA 2: Collisioni Righe DISPARI (1, 3, 5, 7...)
		// ---------------------------------------------------------
        runTasks(
                IntRange.withStep(1, totalRows, 2),
                this::processRowCollisions
        );
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

    public <T> void runTasks(List<T> items, Consumer<T> action) {
		var work = new ArrayList<Callable<Void>>();
		for (var item : items) {
			work.add(() -> {
				action.accept(item);
				return null;
			});
		}
		try {
			exec.invokeAll(work);
		} catch (Exception ignored) {}
	}

    public <T> void distributeLinearTasks(List<T> items, Consumer<T> action) {
		int totalSize = items.size();
		int nActualTasks = Math.min(nTasks, totalSize);
		int workAmount = totalSize / nActualTasks;

		runTasks(IntRange.until(nActualTasks), taskIndex -> {
			int start = taskIndex * workAmount;
			// L'ultima task prende tutti gli elementi fino alla fine della lista,
			// includendo il resto della divisione
			int end = (taskIndex == nActualTasks - 1) ? totalSize : start + workAmount;
			for (int j = start; j < end; j++) {
				action.accept(items.get(j));
			}
		});
	}

	private void setEndGame() {
		int humanScore = gameState.getHumanScore();
		int botScore = gameState.getBotScore();
		String gameResult = humanScore > botScore ? "Human wins! " + humanScore + " - " + botScore
				: botScore > humanScore ? "Bot wins! " + botScore + " - " + humanScore
				: "Draw! " + humanScore + " - " + botScore;
		gameState.endGame(gameResult);
	}

	private void notifyObservers(long tickPerSec) {
		for (var o: observers) {
			o.modelUpdated(board.getBoardViewInfo(), gameState.getGameStateViewInfo(), tickPerSec);
		}
	}

}
