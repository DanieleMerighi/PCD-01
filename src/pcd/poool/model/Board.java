package pcd.poool.model;

import pcd.poool.view.BallViewInfo;
import pcd.poool.view.BoardViewInfo;
import pcd.poool.view.HoleViewInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Board {
    public static final double VELOCITY_FACTOR = 1.5;

    private final List<Ball> balls;
    private final Ball playerBall;
    private final Ball botBall;
    private final Boundary bounds;
    private final List<Hole> holes;
    private int playerScore;
    private int botScore;
    private final Random random;
    private boolean gameOver = false;
    private String gameResult = "";

    public Board(BoardConf conf){
        balls = conf.getSmallBalls();
        playerBall = conf.getPlayerBall();
        botBall = conf.getBotBall();
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

    public synchronized List<Ball> getBalls() {
        return new ArrayList<>(balls);
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

    public synchronized void removeBall(Ball ball) {
        balls.remove(ball);
    }

    public synchronized void addBotScore() {
        botScore++;
    }

    public synchronized void addPlayerScore() {
        playerScore++;
    }

    public synchronized boolean isGameOver() {
        return gameOver;
    }

    public synchronized String getGameResult() {
        return gameResult;
    }

    public synchronized void endGame(String result) {
        gameOver = true;
        gameResult = result;
    }

    public synchronized Boundary getBounds(){
        return this.bounds;
    }

    public synchronized BoardViewInfo getBoardViewInfo() {
        var player = new BallViewInfo(playerBall.getPos(), playerBall.getRadius());
        var bot = new BallViewInfo(botBall.getPos(), botBall.getRadius());
        var ballList = new ArrayList<BallViewInfo>();
        for (var ball : balls) {
            ballList.add(new BallViewInfo(ball.getPos(), ball.getRadius()));
        }
        var holeList = new ArrayList<HoleViewInfo>();
        for (var hole : holes) {
            holeList.add(new HoleViewInfo(hole.pos(), hole.radius()));
        }
        return new BoardViewInfo(player, bot, ballList, holeList, playerScore, botScore);
    }
}
