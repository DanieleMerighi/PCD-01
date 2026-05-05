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
	private long lastUpdateTime = System.currentTimeMillis();
	private static final int MIN_REPAINT_INTERVAL = 10;

	public View(ViewModel model, BoundedBuffer<Cmd> cmdBuffer, int w, int h) {
		this.frame = new ViewFrame(model, cmdBuffer, w, h);
		this.viewModel = model;
	}

	public void display() {
		SwingUtilities.invokeLater(() -> frame.setVisible(true));
	}

	@Override
	public void modelUpdated(BoardViewInfo boardViewInfo, GameStateViewInfo gameStateViewInfo, long framePerSec) {
		long elapsed = System.currentTimeMillis() - lastUpdateTime;

		if (elapsed > MIN_REPAINT_INTERVAL) {
			this.viewModel.update(boardViewInfo, gameStateViewInfo, framePerSec);
			SwingUtilities.invokeLater(this.frame::render);
			lastUpdateTime = System.currentTimeMillis();
		}

	}

	@Override
	public void gameOver(String result) {
		this.viewModel.setGameOver(result);
		SwingUtilities.invokeLater(this.frame::render);
	}
}
