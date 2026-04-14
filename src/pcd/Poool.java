package pcd;

import pcd.controller.ActiveController;
import pcd.model.AutonomousUpdater;
import pcd.model.Board;
import pcd.model.MassiveBoardConf;
import pcd.view.ViewModel;
import pcd.view.View;

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
