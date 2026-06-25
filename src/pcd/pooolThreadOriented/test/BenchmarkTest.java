package pcd.pooolThreadOriented.test;

import pcd.pooolThreadOriented.model.Board;
import pcd.pooolThreadOriented.model.MassiveBoardConf;
import pcd.pooolThreadOriented.model.SimulationWorker;
import pcd.pooolThreadOriented.util.LatchImpl;
import pcd.pooolThreadOriented.util.SynchCell;
import pcd.pooolThreadOriented.util.SynchCellImpl;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BenchmarkTest {
    public static void main(String[] args) {
        int nCores = Runtime.getRuntime().availableProcessors();
        int[] workerConfigs = {1, 2, 4, 6, 8, 10, 11, nCores, nCores + 1};
        int runsPerConfig = 5;

        String csvFile = "benchmark_results_thread.csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile))) {
            writer.println("Workers,Run,MeanMs,MedianMs");
            System.out.println("Starting benchmark. Results in " + csvFile);

            for (int workers : workerConfigs) {
                for (int run = 1; run <= runsPerConfig; run++) {
                    // Forzatura pulizia memoria e attesa deallocazione vecchi Thread
                    System.gc();
                    try { Thread.sleep(150); } catch (InterruptedException ignored) {}

                    System.out.printf("Config [Workers: %2d | Run: %2d] -> Executing... ", workers, run);

                    var coordinator = getSimulationCoordinator(workers);
                    try {
                        coordinator.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    double mean = coordinator.getMeanTimeMs();
                    double median = coordinator.getMedianTimeMs();

                    writer.printf(Locale.US, "%d,%d,%.4f,%.4f\n", workers, run, mean, median);
                    System.out.printf(Locale.US,"Done. Median: %.4f ms (Mean: %.4f ms)\n", median, mean);
                }
            }
        } catch (IOException e) {
            System.err.println("Export error: " + e.getMessage());
        }
    }

    private static SimulationCoordinator getSimulationCoordinator(int workers) {
        var boardConf = new MassiveBoardConf();
        var board = new Board(boardConf);

        var workBuffer = new ArrayList<SynchCell<Runnable>>(workers);
        var workLatch = new LatchImpl(workers);
        for (int i = 0; i < workers; i++) {
            var workCell = new SynchCellImpl<Runnable>();
            workBuffer.add(workCell);
            var worker = new SimulationWorker(workCell, workLatch);
            worker.start();
        }
        var updater = new SimulationCoordinator(board, List.of(), workBuffer, workLatch);

        updater.start();
        return updater;
    }
}
