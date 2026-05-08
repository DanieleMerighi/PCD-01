package pcd.pooolThreadOriented.controller;

import pcd.pooolThreadOriented.model.Board;
import pcd.pooolThreadOriented.model.GameState;
import pcd.pooolThreadOriented.util.BoundedBuffer;

public class KeyboardController extends Thread {

	private final BoundedBuffer<Cmd> cmdBuffer;
	private final Board board;
	private final GameState gameState;
	
	public KeyboardController(Board board, BoundedBuffer<Cmd> cmdBuffer) {
		this.board = board;
		this.gameState = board.getState();
		this.cmdBuffer = cmdBuffer;
	}

	@Override
	public void run() {
		log("started.");
		while (!gameState.isGameOver()) {
			var cmd = cmdBuffer.get();
			cmd.execute(board);
		}
	}
	
	private void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + "][ Controller ] " + msg);
	}

}
