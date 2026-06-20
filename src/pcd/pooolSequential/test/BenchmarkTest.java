package pcd.pooolSequential.test;

import pcd.pooolSequential.model.Board;
import pcd.pooolSequential.model.MassiveBoardConf;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class BenchmarkTest {
    public static void main(String[] args) {
        int runsPerConfig = 10;

        String csvFile = "benchmark_results_sequential.csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile))) {
            writer.println("Workers,Run,TimeMs");
            System.out.println("Starting benchmark. Results in " + csvFile);

            for (int run = 1; run <= runsPerConfig; run++) {
                System.out.printf("Test -> Thread/Workers: %d | Execution: %d... ", 1, run);

                var boardConf = new MassiveBoardConf();
                var board = new Board(boardConf);
                var updater = new SimulationCoordinator(board, List.of());

                updater.start();

                try {
                    updater.join(); // Il main thread attende la fine del ciclo del Coordinator
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                double timeMs = updater.getAverageTimeMs();
                writer.printf("%d,%d,%.4f\n", 1, run, timeMs);
                System.out.printf("Done. Average time: %.4f ms\n", timeMs);
            }
            System.out.println("Benchmark done.");
        } catch (IOException e) {
            System.err.println("Error while writing file CSV: " + e.getMessage());
        }
    }
}
