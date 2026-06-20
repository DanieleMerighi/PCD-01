package pcd.pooolTaskOriented.model;

import pcd.pooolTaskOriented.view.BallViewInfo;
import pcd.pooolTaskOriented.view.GameStateViewInfo;

import java.util.ArrayList;
import java.util.List;

public class GameState {

    private final List<Ball> smallBalls;
    private final List<Ball> allBalls;
    private int humanScore;
    private int botScore;
    private boolean gameOver = false;
    private String gameResult = "";

    public GameState(Ball humanBall, Ball botBall, List<Ball> smallBalls) {
        allBalls = new ArrayList<>();
        allBalls.addAll(List.of(humanBall, botBall));
        allBalls.addAll(smallBalls);
        this.smallBalls = allBalls.subList(2, allBalls.size()); // Dynamic view
    }

    public synchronized List<Ball> getAllBalls() {
        return List.copyOf(allBalls);
    }

    public synchronized void removeSmallBall(Ball ball) {
        smallBalls.remove(ball);
    }

    public synchronized boolean isSmallBallEmpty() {
        return smallBalls.isEmpty();
    }

    public synchronized List<Ball> getSmallBalls() {
        return List.copyOf(smallBalls);
    }

    public synchronized List<Ball> getMainBalls() {
        return List.of(allBalls.get(0), allBalls.get(1));
    }

    public synchronized int getHumanScore() {
        return humanScore;
    }

    public synchronized int getBotScore() {
        return botScore;
    }

    public synchronized void addHumanScore() {
        humanScore++;
    }

    public synchronized void addBotScore() {
        botScore++;
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

    public synchronized GameStateViewInfo getGameStateViewInfo() {
        var balls = new ArrayList<BallViewInfo>();
        for (var ball : smallBalls) {
            balls.add(new BallViewInfo(ball.getPos(), ball.getRadius()));
        }
        return new GameStateViewInfo(balls, humanScore, botScore);
    }

}
