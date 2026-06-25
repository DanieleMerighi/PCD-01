package pcd.pooolThreadOrientedJpf.model;

import pcd.pooolThreadOrientedJpf.util.AtomicList;
import pcd.pooolThreadOrientedJpf.util.AtomicListImpl;

import java.util.ArrayList;
import java.util.List;

public class Board {

    private final Boundary bounds;
    private final List<Hole> holes;
    private final Ball humanBall;
    private final Ball botBall;
    private final AtomicList<Ball> smallBalls;
    private final GameState state;

    public Board(BoardConf conf) {
        humanBall = conf.getHumanBall();
        botBall = conf.getBotBall();
        smallBalls = new AtomicListImpl<>(conf.getSmallBalls());
        state = new GameState();
        bounds = conf.getBoardBoundary();
        holes = conf.getHoles();
    }

    public List<Ball> getAllBalls() {
        var l = new ArrayList<>(List.of(humanBall, botBall));
        l.addAll(smallBalls.getAll());
        return List.copyOf(l);
    }

    public void removeSmallBall(Ball ball) {
        smallBalls.removeElement(ball);
    }

    public boolean isSmallBallEmpty() {
        return smallBalls.isEmpty();
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
