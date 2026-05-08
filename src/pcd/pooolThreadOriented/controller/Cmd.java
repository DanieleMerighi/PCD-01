package pcd.pooolThreadOriented.controller;

import pcd.pooolThreadOriented.model.Board;

public interface Cmd {

	void execute(Board board);

}
