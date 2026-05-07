package pcd.poool;

import pcd.poool.controller.KeyboardController;
import pcd.poool.controller.Cmd;
import pcd.poool.model.*;
import pcd.poool.util.BoundedBufferImpl;
import pcd.poool.util.WorkBufferImpl;
import pcd.poool.view.ViewModel;
import pcd.poool.view.View;

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
        var workBuffer = new WorkBufferImpl(10000);
        var updater = new SimulationCoordinator(board, List.of(view), workBuffer, nWorker);

        for (int i = 0; i < nWorker; i++) {
            var worker = new SimulationWorker(gameState, workBuffer);
            worker.start();
        }

        var botUpdater = new BotUpdater(board);

        updater.start();
        botUpdater.start();

        view.display();
    }
}
