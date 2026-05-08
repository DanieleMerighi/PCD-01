package pcd.pooolThreadOriginal.controller;

import pcd.pooolThreadOriginal.model.Board;
import pcd.pooolThreadOriginal.model.Direction;

public class ArrowKeyCmd implements Cmd {

    private final Direction direction;

    public ArrowKeyCmd(Direction direction) {
        this.direction = direction;
    }

    @Override
    public void execute(Board board) {
        board.kickHumanBall(direction);
    }

}
