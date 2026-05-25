package pcd.pooolThreadOriented;

import pcd.pooolThreadOriented.controller.KeyboardController;
import pcd.pooolThreadOriented.controller.Cmd;
import pcd.pooolThreadOriented.model.*;
import pcd.pooolThreadOriented.util.BoundedBufferImpl;
import pcd.pooolThreadOriented.util.LatchImpl;
import pcd.pooolThreadOriented.util.SynchCell;
import pcd.pooolThreadOriented.util.SynchCellImpl;
import pcd.pooolThreadOriented.view.ViewModel;
import pcd.pooolThreadOriented.view.View;

import java.util.ArrayList;
import java.util.List;

public class Poool {
    public static void main(String[] argv) {

        // var boardConf = new MinimalBoardConf();
        // var boardConf = new LargeBoardConf();
        var boardConf = new MassiveBoardConf();

        var board = new Board(boardConf);
        var gameState = board.getState();

        var cmdBuffer = new BoundedBufferImpl<Cmd>(100);

        var controller = new KeyboardController(board, cmdBuffer);
        controller.start();

        var viewModel = new ViewModel(board.getBoardViewInfo(), gameState.getGameStateViewInfo());
        var view = new View(viewModel, cmdBuffer, 1200, 800);

        int nWorker = Runtime.getRuntime().availableProcessors() + 1;
        var workBuffer = new ArrayList<SynchCell<Runnable>>(nWorker);
        var workLatch = new LatchImpl(nWorker);
        for (int i = 0; i < nWorker; i++) {
            var workCell = new SynchCellImpl<Runnable>();
            workBuffer.add(workCell);
            var worker = new SimulationWorker(workCell, workLatch);
            worker.start();
        }
        var updater = new SimulationCoordinator(board, List.of(view), workBuffer, workLatch);

        var botUpdater = new BotUpdater(board);

        updater.start();
        botUpdater.start();

        view.display();
    }
}
