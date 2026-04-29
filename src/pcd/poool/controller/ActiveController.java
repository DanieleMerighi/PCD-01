package pcd.poool.controller;

import pcd.poool.model.Board;
import pcd.poool.util.BoundedBuffer;

public class ActiveController extends Thread {

	private final BoundedBuffer<Cmd> cmdBuffer;
	private final Board board;
	
	public ActiveController(Board board, BoundedBuffer<Cmd> cmdBuffer) {
		this.cmdBuffer = cmdBuffer;
		this.board = board;
	}

	@Override
	public void run() {
		log("started.");
		while (true) {
			//log("Waiting for cmds ");
			var cmd = cmdBuffer.get();
			//log("new cmd fetched: " + cmd);
			cmd.execute(board);
		}
	}
	
	private void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + "][ Controller ] " + msg);
	}
}
