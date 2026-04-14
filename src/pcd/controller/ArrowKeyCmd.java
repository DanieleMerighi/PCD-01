package pcd.controller;

import pcd.model.Board;

public class ArrowKeyCmd implements Cmd {
    private final Direction direction;

    public ArrowKeyCmd(Direction direction) {
        this.direction = direction;
    }

    @Override
    public void execute(Board board) {
        board.kickPlayerBall(direction);
    }
}
