package pcd.poool;

import pcd.poool.controller.ActiveController;
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

        Board board = new Board(boardConf);
        GameState gameState = new GameState();

        var cmdBuffer = new BoundedBufferImpl<Cmd>(100);

        var controller = new ActiveController(board, gameState, cmdBuffer);
        controller.start();

        var viewModel = new ViewModel(board.getBoardViewInfo(), gameState.getGameStateViewInfo());
        var view = new View(viewModel, cmdBuffer, 1200, 800);

        var workBuffer = new WorkBufferImpl(10000);
        var updater = new SimulationCoordinator(board, gameState, List.of(view), workBuffer);

        int nWorker = Runtime.getRuntime().availableProcessors() + 1;
        for (int i = 0; i < nWorker; i++) {
            var worker = new SimulationWorker(gameState, workBuffer);
            worker.start();
        }

        var botUpdater = new BotUpdater(board, gameState);

        updater.start();
        botUpdater.start();


        view.display();
    }
}
