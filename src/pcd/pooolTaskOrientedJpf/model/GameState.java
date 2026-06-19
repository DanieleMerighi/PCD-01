package pcd.pooolTaskOrientedJpf.model;

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
        allBalls = new ArrayList<Ball>();
        allBalls.add(humanBall);
        allBalls.add(botBall);
        allBalls.addAll(smallBalls);
        this.smallBalls = allBalls.subList(2, allBalls.size());
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
}
