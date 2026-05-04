package pcd.poool.view;

import pcd.poool.controller.Cmd;
import pcd.poool.model.Ball;
import pcd.poool.model.BoardObserver;
import pcd.poool.model.Hole;
import pcd.poool.util.BoundedBuffer;

import javax.swing.*;
import java.util.List;

public class View implements BoardObserver {

	private final ViewFrame frame;
	private final ViewModel viewModel;


	public View(ViewModel model, BoundedBuffer<Cmd> cmdBuffer, int w, int h) {
		this.frame = new ViewFrame(model, cmdBuffer, w, h);
		this.viewModel = model;
	}

	public void display() {
		SwingUtilities.invokeLater(() -> frame.setVisible(true));
	}

	@Override
	public void modelUpdated(BoardViewInfo boardViewInfo, int framePerSec) {
		SwingUtilities.invokeLater(() -> {
			this.viewModel.update(boardViewInfo, framePerSec);
			this.frame.render();
		});
	}

	@Override
	public void gameOver(String result) {
		SwingUtilities.invokeLater(() -> {
			this.viewModel.setGameOver(result);
			this.frame.render();
		});
	}
}
