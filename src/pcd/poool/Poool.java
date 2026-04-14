package pcd.poool;

import pcd.poool.controller.ActiveController;
import pcd.poool.model.AutonomousUpdater;
import pcd.poool.model.Board;
import pcd.poool.model.MassiveBoardConf;
import pcd.poool.view.ViewModel;
import pcd.poool.view.View;

public class Poool {
    public static void main(String[] argv) {

        // var boardConf = new MinimalBoardConf();
        // var boardConf = new LargeBoardConf();
        var boardConf = new MassiveBoardConf();

        Board board = new Board();
        board.init(boardConf);

        var controller = new ActiveController(board);

        var viewModel = new ViewModel();
        var view = new View(viewModel, controller, 1200, 800);

        controller.start();

        var updater = new AutonomousUpdater(board);
        updater.addObserver(view);
        updater.start();

        view.display();
    }
}
