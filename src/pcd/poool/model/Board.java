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
    private volatile boolean gameOver = false;
    private String gameResult = "";

    public Board(BoardConf conf){
        balls = conf.getSmallBalls();
        playerBall = conf.getPlayerBall();
        botBall = conf.getBotBall();
        bounds = conf.getBoardBoundary();
        holes = conf.getHoles();
        random = new Random(System.currentTimeMillis());
    }
    
    public synchronized void updateState(long dt) {
    	playerBall.updateState(dt, this);
        botBall.updateState(dt, this);
    	
    	for (var b: balls) {
    		b.updateState(dt, this);
    	}
        for (var hole: holes) {
            if (playerBall.resolveHole(hole)) {
                gameOver = true;
                gameResult = "Bot wins! Player fell in a hole.";
                return;
            }
            if (botBall.resolveHole(hole)) {
                gameOver = true;
                gameResult = "Player wins! Bot fell in a hole.";
                return;
            }
            for (var b: List.copyOf(balls)) {
                if (b.resolveHole(hole)) {
                    switch (b.getHitCredit()){
                        case BOT -> botScore++;
                        case PLAYER -> playerScore++;
                    }
                    balls.remove(b);
                }
            }
        }
        if (balls.isEmpty()) {
            gameOver = true;
            gameResult = playerScore > botScore ? "Player wins! " + playerScore + " - " + botScore
                       : botScore > playerScore ? "Bot wins! " + botScore + " - " + playerScore
                       : "Draw! " + playerScore + " - " + botScore;
        }
    	for (int i = 0; i < balls.size() - 1; i++) {
            for (int j = i + 1; j < balls.size(); j++) {
                if (Ball.resolveCollision(balls.get(i), balls.get(j))) {
                    balls.get(i).setHitCredit(HitCredit.NONE);
                    balls.get(j).setHitCredit(HitCredit.NONE);
                }
            }
        }
    	for (var b: balls) {
    		if (Ball.resolveCollision(playerBall, b)) {
                b.setHitCredit(HitCredit.PLAYER);
            }
            if (Ball.resolveCollision(botBall, b)) {
                b.setHitCredit(HitCredit.BOT);
            }
    	}
        Ball.resolveCollision(playerBall, botBall);
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

    public boolean isGameOver() {
        return gameOver;
    }

    public synchronized String getGameResult() {
        return gameResult;
    }

    public synchronized Boundary getBounds(){
        return this.bounds;
    }

    public synchronized BoardViewInfo getBoardViewInfo() {
        var player = new BallViewInfo(playerBall.getPos(), playerBall.getRadius());
        var bot = new BallViewInfo(botBall.getPos(), botBall.getRadius());
        var ballList = new ArrayList<BallViewInfo>();
        for (var ball: balls) {
            ballList.add(new BallViewInfo(ball.getPos(), ball.getRadius()));
        }
        var holeList = new ArrayList<HoleViewInfo>();
        for (var hole: holes) {
            holeList.add(new HoleViewInfo(hole.pos(), hole.radius()));
        }
        return new BoardViewInfo(player, bot, ballList, holeList, playerScore, botScore);
    }
}
