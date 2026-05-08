package pcd.pooolTaskOriented.controller;

import pcd.pooolTaskOriented.model.Board;
import pcd.pooolTaskOriented.model.Direction;

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
