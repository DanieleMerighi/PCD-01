package pcd.poool.controller;

import pcd.poool.model.Board;
import pcd.poool.model.GameState;
import pcd.poool.util.BoundedBuffer;

public class ActiveController extends Thread {

	private final BoundedBuffer<Cmd> cmdBuffer;
	private final Board board;
	private final GameState gameState;
	
	public ActiveController(Board board, GameState gameState, BoundedBuffer<Cmd> cmdBuffer) {
		this.cmdBuffer = cmdBuffer;
		this.board = board;
		this.gameState = gameState;
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
