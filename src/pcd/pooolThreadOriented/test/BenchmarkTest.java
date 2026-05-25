package pcd.pooolThreadOriented.test;

import pcd.pooolThreadOriented.model.Board;
import pcd.pooolThreadOriented.model.MassiveBoardConf;
import pcd.pooolThreadOriented.model.SimulationWorker;
import pcd.pooolThreadOriented.util.LatchImpl;
import pcd.pooolThreadOriented.util.SynchBox;
import pcd.pooolThreadOriented.util.SynchBoxImpl;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class BenchmarkTest {
    public static void main(String[] args) {
        int nCores = Runtime.getRuntime().availableProcessors();
        int[] workerConfigs = {1, 2, 4, 8, nCores, nCores + 1};
        int runsPerConfig = 5;

        String csvFile = "benchmark_results_thread.csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile))) {
            writer.println("Workers,Run,TimeMs");
            System.out.println("Starting benchmark. Results in " + csvFile);

            for (int workers : workerConfigs) {
                for (int run = 1; run <= runsPerConfig; run++) {
                    System.out.printf("Test -> Thread/Workers: %d | Execution: %d... ", workers, run);

                    var boardConf = new MassiveBoardConf();
                    var board = new Board(boardConf);
                    var gameState = board.getState();

                    var workBuffer = new ArrayList<SynchBox<Runnable>>(workers);
                    var workLatch = new LatchImpl(workers);
                    for (int i = 0; i < workers; i++) {
                        var workBox = new SynchBoxImpl<Runnable>();
                        workBuffer.add(workBox);
                        var worker = new SimulationWorker(workBox, workLatch);
                        worker.start();
                    }
                    var updater = new SimulationCoordinator(board, List.of(), workBuffer, workLatch);

                    updater.start();

                    try {
                        updater.join(); // Il main thread attende la fine del ciclo del Coordinator
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    double timeMs = updater.getAverageTimeMs();
                    writer.printf("%d,%d,%.4f\n", workers, run, timeMs);
                    System.out.printf("Done. Average time: %.4f ms\n", timeMs);
                }
            }
            System.out.println("Benchmark done.");
        } catch (IOException e) {
            System.err.println("Error while writing file CSV: " + e.getMessage());
        }
    }
}
