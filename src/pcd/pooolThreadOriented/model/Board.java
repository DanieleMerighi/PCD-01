package pcd.pooolThreadOriented.model;

import pcd.pooolThreadOriented.util.AtomicList;
import pcd.pooolThreadOriented.util.AtomicListImpl;
import pcd.pooolThreadOriented.util.AtomicReference;
import pcd.pooolThreadOriented.util.AtomicReferenceImpl;
import pcd.pooolThreadOriented.view.BallViewInfo;
import pcd.pooolThreadOriented.view.BoardViewInfo;
import pcd.pooolThreadOriented.view.HoleViewInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Board {

    private static final double KICK_SPEED = 1.5;

    private final Boundary bounds;
    private final List<Hole> holes;
    private final Ball humanBall;
    private final Ball botBall;
    private final AtomicReference<V2d> humanKick;
    private final AtomicReference<V2d> botKick;
    private final AtomicList<Ball> smallBalls;
    private final GameState state;
    private final Random random;

    public Board(BoardConf conf) {
        humanBall = conf.getHumanBall();
        botBall = conf.getBotBall();
        humanKick = new AtomicReferenceImpl<>(null);
        botKick = new AtomicReferenceImpl<>(null);
        smallBalls = new AtomicListImpl<>(conf.getSmallBalls());
        state = new GameState();
        bounds = conf.getBoardBoundary();
        holes = conf.getHoles();
        random = new Random(System.currentTimeMillis());
    }

    public void kickHumanBall(Direction direction) {
        var velocity = direction.getVector().mul(KICK_SPEED);
        humanKick.set(velocity);
    }

    public void kickBotBall() {
        var botPos = botBall.getPos();
        double angle;
        int attempts = 0;
        do {
            angle = random.nextDouble() * Math.PI * 2;
            attempts++;
        } while (attempts < 20 && isAngleDangerous(angle, botPos));
        var v = new V2d(Math.cos(angle), Math.sin(angle)).mul(KICK_SPEED);
        botKick.set(v);
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

    public void applyHumanKick() {
        var vel = humanKick.getAndSet(null);
        if (vel != null) {
            humanBall.kick(vel);
        }
    }

    public void applyBotKick() {
        var vel = botKick.getAndSet(null);
        if (vel != null) {
            botBall.kick(vel);
        }
    }

    public Boundary getBounds(){
        return this.bounds;
    }

    public List<Hole> getHoles() {
        return List.copyOf(holes);
    }

    public GameState getState() {
        return this.state;
    }

    public BoardViewInfo getBoardViewInfo() {
        var humanBall = new BallViewInfo(this.humanBall.getPos(), this.humanBall.getRadius());
        var botBall = new BallViewInfo(this.botBall.getPos(), this.botBall.getRadius());
        var smallBalls = new ArrayList<BallViewInfo>();
        for (var ball : this.smallBalls.getAll()) {
            smallBalls.add(new BallViewInfo(ball.getPos(), ball.getRadius()));
        }
        var holes = new ArrayList<HoleViewInfo>();
        for (var hole : this.holes) {
            holes.add(new HoleViewInfo(hole.pos(), hole.radius()));
        }
        return new BoardViewInfo(humanBall, botBall, smallBalls, holes);
    }

    private boolean isAngleDangerous(double angle, P2d botPos) {
        for (var hole : holes) {
            double dx = hole.pos().x() - botPos.x();
            double dy = hole.pos().y() - botPos.y();
            double dist = Math.hypot(dx, dy);
            double toHole = Math.atan2(dy, dx); // [-π, π]
            double diff = ((angle - toHole + Math.PI) % (2 * Math.PI)) - Math.PI;
            double dangerHalfAngle = Math.min(Math.PI * 0.6, 0.5 / dist);
            if (Math.abs(diff) < dangerHalfAngle) return true;
        }
        return false;
    }

}
