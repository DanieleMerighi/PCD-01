package pcd.poool;

import pcd.poool.controller.ActiveController;
import pcd.poool.controller.Cmd;
import pcd.poool.model.AutonomousUpdater;
import pcd.poool.model.Board;
import pcd.poool.model.MinimalBoardConf;
import pcd.poool.model.LargeBoardConf;
import pcd.poool.model.MassiveBoardConf;
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

        var viewModel = new ViewModel();
        var view = new View(viewModel, cmdBuffer, 1200, 800);

        controller.start();

        var updater = new AutonomousUpdater(board, List.of(view));
        updater.start();

        view.display();
    }
}
