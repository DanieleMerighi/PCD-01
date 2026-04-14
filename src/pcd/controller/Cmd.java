package pcd.controller;

import pcd.model.Board;

public interface Cmd {
	void execute(Board board);
}
