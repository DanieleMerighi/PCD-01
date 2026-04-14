package pcd.poool.view;


import pcd.poool.controller.ActiveController;
import pcd.poool.model.Board;
import pcd.poool.model.BoardObserver;

import javax.swing.*;

public class View implements BoardObserver {

	private ViewFrame frame;
	private ViewModel viewModel;
	private ActiveController controller;
	
	public View(ViewModel model, ActiveController controller, int w, int h) {
		this.controller = controller;
		frame = new ViewFrame(model, controller, w, h);
		this.viewModel = model;
	}

	public void display() {
		SwingUtilities.invokeLater(() -> {
			frame.setVisible(true);
		});
	}

	@Override
	public void modelUpdated(Board board, int framePerSec) {
		viewModel.update(board, framePerSec);
		frame.render();
	}
}
