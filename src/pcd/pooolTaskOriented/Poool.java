package pcd.pooolTaskOriented;

import pcd.pooolTaskOriented.controller.Cmd;
import pcd.pooolTaskOriented.controller.KeyboardController;
import pcd.pooolTaskOriented.model.*;
import pcd.pooolTaskOriented.util.BoundedBufferImpl;
import pcd.pooolTaskOriented.view.View;
import pcd.pooolTaskOriented.view.ViewModel;

import java.util.List;
import java.util.concurrent.Executors;

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
        var exec = Executors.newFixedThreadPool(nWorker);
        var updater = new SimulationCoordinator(board, List.of(view), exec, nWorker);

        var botUpdater = new BotUpdater(board);

        updater.start();
        botUpdater.start();

        view.display();
    }
}
