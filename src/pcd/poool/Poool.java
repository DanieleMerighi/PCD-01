package pcd.poool;

import pcd.poool.controller.ActiveController;
import pcd.poool.controller.Cmd;
import pcd.poool.model.*;
import pcd.poool.util.BoundedBufferImpl;
import pcd.poool.view.ViewModel;
import pcd.poool.view.View;

import java.util.List;

public class Poool {
    public static void main(String[] argv) {

        // var boardConf = new MinimalBoardConf();
        var boardConf = new LargeBoardConf();
        // var boardConf = new MassiveBoardConf();

        Board board = new Board(boardConf);

        var cmdBuffer = new BoundedBufferImpl<Cmd>(100);

        var controller = new ActiveController(board, cmdBuffer);
        controller.start();

        var viewModel = new ViewModel();
        var view = new View(viewModel, cmdBuffer, 1200, 800);

        var updater = new AutonomousUpdater(board, List.of(view));
        var botUpdater = new BotUpdater(board);

        updater.start();
        botUpdater.start();


        view.display();
    }
}
