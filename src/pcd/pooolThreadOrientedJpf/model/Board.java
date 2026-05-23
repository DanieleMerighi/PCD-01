package pcd.pooolThreadOrientedJpf.model;

import java.util.List;

public class Board {

    private static final double KICK_SPEED = 1.5;

    private final Boundary bounds;
    private final List<Hole> holes;
    private final Ball humanBall;
    private final Ball botBall;
    private final GameState state;

    public Board(BoardConf conf) {
        humanBall = conf.getHumanBall();
        botBall = conf.getBotBall();
        state = new GameState(humanBall, botBall, conf.getSmallBalls());
        bounds = conf.getBoardBoundary();
        holes = conf.getHoles();
    }

    public void kickHumanBall(Direction direction) {
        V2d velocity = direction.getVector().mul(KICK_SPEED);
        humanBall.kick(velocity);
    }

    public Boundary getBounds() {
        return this.bounds;
    }

    public List<Hole> getHoles() {
        return List.copyOf(holes);
    }

    public GameState getState() {
        return this.state;
    }
}
