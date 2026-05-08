package pcd.pooolSequential.controller;

import pcd.pooolSequential.model.Board;

public interface Cmd {

	void execute(Board board);

}
