package pcd.poool.model;

import pcd.poool.view.BallViewInfo;
import pcd.poool.view.BoardViewInfo;
import pcd.poool.view.HoleViewInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Board {
    public static final double VELOCITY_FACTOR = 1.5;

    private final List<Ball> smallBalls;
    private final Ball playerBall;
    private final Ball botBall;
    private final List<Ball> allBalls;
    private final Boundary bounds;
    private final List<Hole> holes;
    private final Random random;

    public Board(BoardConf conf){
        playerBall = conf.getPlayerBall();
        botBall = conf.getBotBall();
        allBalls = new ArrayList<>();
        allBalls.addAll(List.of(playerBall, botBall));
        allBalls.addAll(conf.getSmallBalls());
        smallBalls = allBalls.subList(2, allBalls.size());
        bounds = conf.getBoardBoundary();
        holes = conf.getHoles();
        random = new Random(System.currentTimeMillis());
    }

    public synchronized Ball getPlayerBall() {
        return playerBall;
    }

    public synchronized Ball getBotBall() {
        return botBall;
    }

    public synchronized List<Ball> getSmallBalls() {
        return new ArrayList<>(smallBalls);
    }

    public synchronized List<Ball> getAllBalls() {
        return new ArrayList<>(allBalls);
    }

    public synchronized List<Hole> getHoles() {
        return new ArrayList<>(holes);
    }

    public synchronized void kickPlayerBall(Direction direction) {
        var velocity = direction.getVector().mul(VELOCITY_FACTOR);
        playerBall.kick(velocity);
    }

    public synchronized void kickBotBall() {
        var botPos = botBall.getPos();
        double angle;
        int attempts = 0;
        do {
            angle = random.nextDouble() * Math.PI * 2;
            attempts++;
        } while (attempts < 20 && isAngleDangerous(angle, botPos));
        var v = new V2d(Math.cos(angle), Math.sin(angle)).mul(1.5);
        botBall.kick(v);
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

    public synchronized void removeSmallBall(Ball ball) {
        smallBalls.remove(ball);
    }

    public Boundary getBounds(){
        return this.bounds;
    }

    public synchronized BoardViewInfo getBoardViewInfo() {
        var player = new BallViewInfo(playerBall.getPos(), playerBall.getRadius());
        var bot = new BallViewInfo(botBall.getPos(), botBall.getRadius());
        var ballList = new ArrayList<BallViewInfo>();
        for (var ball : smallBalls) {
            ballList.add(new BallViewInfo(ball.getPos(), ball.getRadius()));
        }
        var holeList = new ArrayList<HoleViewInfo>();
        for (var hole : holes) {
            holeList.add(new HoleViewInfo(hole.pos(), hole.radius()));
        }
        return new BoardViewInfo(player, bot, ballList, holeList);
    }
}
