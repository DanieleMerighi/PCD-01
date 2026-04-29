package pcd.poool.controller;

import pcd.poool.model.Board;
import pcd.poool.model.Direction;

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
