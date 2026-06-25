package pcd.pooolTaskOriented.test;

import pcd.pooolTaskOriented.model.Board;
import pcd.pooolTaskOriented.model.MassiveBoardConf;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

public class BenchmarkTest {
    public static void main(String[] args) {
        int nCores = Runtime.getRuntime().availableProcessors();
        int[] workerConfigs = {1, 2, 4, 6, 8, 10, 11, nCores, nCores + 1};
        int runsPerConfig = 5;

        String csvFile = "benchmark_results_task.csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile))) {
            writer.println("Workers,Run,MeanMs,MedianMs");
            System.out.println("Starting rigorous Task-Oriented benchmark. Results in " + csvFile);

            for (int workers : workerConfigs) {
                for (int run = 1; run <= runsPerConfig; run++) {
                    // Isolamento della memoria e shutdown del ThreadPool precedente
                    System.gc();
                    try { Thread.sleep(150); } catch (InterruptedException ignored) {}

                    System.out.printf("Test -> TaskPool/Workers: %2d | Execution: %2d... ", workers, run);

                    var boardConf = new MassiveBoardConf();
                    var board = new Board(boardConf);
                    var updater = new SimulationCoordinator(board, List.of(), workers, 100);

                    updater.start();

                    try {
                        updater.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    double mean = updater.getMeanTimeMs();
                    double median = updater.getMedianTimeMs();

                    writer.printf(Locale.US, "%d,%d,%.4f,%.4f\n", workers, run, mean, median);
                    System.out.printf(Locale.US,"Done. Median: %.4f ms (Mean: %.4f ms)\n", median, mean);
                }
            }
            System.out.println("Task-Oriented benchmark completed.");
        } catch (IOException e) {
            System.err.println("Error while writing file CSV: " + e.getMessage());
        }
    }
}