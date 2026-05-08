package pcd.pooolSequential.controller;

import pcd.pooolSequential.model.Board;
import pcd.pooolSequential.model.Direction;

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
