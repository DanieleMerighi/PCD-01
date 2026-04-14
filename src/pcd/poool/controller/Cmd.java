package pcd.poool.controller;

import pcd.poool.model.Board;

public interface Cmd {
	void execute(Board board);
}
