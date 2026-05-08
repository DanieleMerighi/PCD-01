package pcd.pooolThreadOriented.view;

import pcd.pooolThreadOriented.controller.Cmd;
import pcd.pooolThreadOriented.model.BoardObserver;
import pcd.pooolThreadOriented.util.BoundedBuffer;

import javax.swing.*;

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
	public void modelUpdated(BoardViewInfo boardViewInfo, GameStateViewInfo gameStateViewInfo, long tickPerSec) {
		long elapsed = System.currentTimeMillis() - lastUpdateTime;

		if (elapsed > MIN_REPAINT_INTERVAL) {
			this.viewModel.update(boardViewInfo, gameStateViewInfo, tickPerSec);
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
