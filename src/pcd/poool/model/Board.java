package pcd.poool.model;

import java.util.List;

public class Board {
    public static final double VELOCITY_FACTOR = 1.5;

    private final List<Ball> balls;
    private final Ball playerBall;
    private final Boundary bounds;
    
    public Board(BoardConf conf){
        balls = conf.getSmallBalls();
        playerBall = conf.getPlayerBall();
        bounds = conf.getBoardBoundary();
    }
    
    public synchronized void updateState(long dt) {
    	playerBall.updateState(dt, this);
    	
    	for (var b: balls) {
    		b.updateState(dt, this);
    	}
    	for (int i = 0; i < balls.size() - 1; i++) {
            for (int j = i + 1; j < balls.size(); j++) {
                Ball.resolveCollision(balls.get(i), balls.get(j));
            }
        }
    	for (var b: balls) {
    		Ball.resolveCollision(playerBall, b);
    	}
    }

    public synchronized void kickPlayerBall(Direction direction) {
        var velocity = direction.getVector().mul(VELOCITY_FACTOR);
        if (playerBall.getVel().abs() < 0.05)
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
}
