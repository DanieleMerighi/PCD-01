package pcd.pooolSequential;

import pcd.pooolSequential.controller.Cmd;
import pcd.pooolSequential.util.BoundedBufferImpl;
import pcd.pooolSequential.view.View;
import pcd.pooolSequential.view.ViewModel;
import pcd.pooolSequential.controller.KeyboardController;
import pcd.pooolSequential.model.SimulationCoordinator;

import java.util.List;

public class Poool {
    public static void main(String[] argv) {

        // var boardConf = new MinimalBoardConf();
        // var boardConf = new LargeBoardConf();
        var boardConf = new pcd.pooolSequential.model.MassiveBoardConf();

        var board = new pcd.pooolSequential.model.Board(boardConf);
        var gameState = board.getState();

        var cmdBuffer = new BoundedBufferImpl<Cmd>(100);

        var controller = new KeyboardController(board, cmdBuffer);
        controller.start();

        var viewModel = new ViewModel(board.getBoardViewInfo(), gameState.getGameStateViewInfo());
        var view = new View(viewModel, cmdBuffer, 1200, 800);

        var updater = new SimulationCoordinator(board, List.of(view));

        var botUpdater = new pcd.pooolSequential.model.BotUpdater(board);

        updater.start();
        botUpdater.start();

        view.display();
    }
}
