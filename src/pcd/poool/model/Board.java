package pcd.poool.model;

import pcd.poool.view.BallViewInfo;
import pcd.poool.view.BoardViewInfo;
import pcd.poool.view.HoleViewInfo;

import java.util.ArrayList;
import java.util.List;

public class Board {
    public static final double VELOCITY_FACTOR = 1.5;

    private final List<Ball> balls;
    private final Ball playerBall;
    private final Boundary bounds;
    private final List<Hole> holes;
    private int playerScore;
    private int botScore;

    public Board(BoardConf conf){
        balls = conf.getSmallBalls();
        playerBall = conf.getPlayerBall();
        bounds = conf.getBoardBoundary();
        holes = conf.getHoles();
    }
    
    public synchronized void updateState(long dt) {
    	playerBall.updateState(dt, this);
    	
    	for (var b: balls) {
    		b.updateState(dt, this);
    	}
        for (var hole: holes) {
            if (playerBall.resolveHole(hole)) {
                //return MORTE FINE PARTITAAAAA
            }
            for (var b: List.copyOf(balls)) {
                if (b.resolveHole(hole)) {
                    switch (b.getHitCredit()){
                        case BOT -> botScore++;
                        case PLAYER -> playerScore++;
                    }
                    balls.remove(b);
                    System.out.println("removed ballll. Balls: " + balls.size());
                }
            }
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
    	}
    }

    public synchronized void kickPlayerBall(Direction direction) {
        var velocity = direction.getVector().mul(VELOCITY_FACTOR);
        playerBall.kick(velocity);

    }
    
    public synchronized List<Ball> getBalls(){
    	return balls;
    }
    
    public synchronized Ball getPlayerBall() {
    	return playerBall;
    }
    
    public synchronized Boundary getBounds(){
        return bounds;
    }

    public synchronized BoardViewInfo getBoardViewInfo() {
        var player = new BallViewInfo(playerBall.getPos(), playerBall.getRadius());
        var ballList = new ArrayList<BallViewInfo>();
        for (var ball: balls) {
            ballList.add(new BallViewInfo(ball.getPos(), ball.getRadius()));
        }
        var holeList = new ArrayList<HoleViewInfo>();
        for (var hole: holes) {
            holeList.add(new HoleViewInfo(hole.pos(), hole.radius()));
        }
        return new BoardViewInfo(player, ballList, holeList, playerScore, botScore);
    }
}
