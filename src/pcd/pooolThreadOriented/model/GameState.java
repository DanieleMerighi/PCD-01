package pcd.pooolThreadOriented.model;

import pcd.pooolThreadOriented.util.AtomicList;
import pcd.pooolThreadOriented.util.AtomicListImpl;
import pcd.pooolThreadOriented.util.AtomicReference;
import pcd.pooolThreadOriented.util.AtomicReferenceImpl;
import pcd.pooolThreadOriented.view.BallViewInfo;
import pcd.pooolThreadOriented.view.GameStateViewInfo;

import java.util.ArrayList;
import java.util.List;

public class GameState {

    private final AtomicList<Ball> smallBalls;
    private final AtomicList<Ball> allBalls;
    private final AtomicReference<Integer> humanScore = new AtomicReferenceImpl<>(0);
    private final AtomicReference<Integer> botScore = new AtomicReferenceImpl<>(0);
    private final AtomicReference<Boolean> gameOver = new AtomicReferenceImpl<>(false);
    private final AtomicReference<String> gameResult = new AtomicReferenceImpl<>("");

    public GameState(Ball humanBall, Ball botBall, List<Ball> smallBalls) {
        allBalls = new AtomicListImpl<>(List.of(humanBall, botBall));
        allBalls.addAll(smallBalls);
        this.smallBalls = allBalls.subList(2, allBalls.size()); // Dynamic view
    }

    public List<Ball> getAllBalls() {
        return allBalls.getAll();
    }

    public void removeSmallBall(Ball ball) {
        smallBalls.removeElement(ball);
    }

    public boolean isSmallBallEmpty() {
        return smallBalls.isEmpty();
    }

    public List<Ball> getSmallBalls() {
        return smallBalls.getAll();
    }

    public List<Ball> getMainBalls() {
        return List.of(allBalls.get(0), allBalls.get(1));
    }

    public int getHumanScore() {
        return humanScore.get();
    }

    public int getBotScore() {
        return botScore.get();
    }

    public void addHumanScore() {
        humanScore.map(value -> value + 1);
    }

    public void addBotScore() {
        botScore.map(value -> value + 1);
    }

    public boolean isGameOver() {
        return gameOver.get();
    }

    public String getGameResult() {
        return gameResult.get();
    }

    public void endGame(String result) {
        gameOver.set(true);
        gameResult.set(result);
    }

    public GameStateViewInfo getGameStateViewInfo() {
        var balls = new ArrayList<BallViewInfo>();
        for (var ball : smallBalls.getAll()) {
            balls.add(new BallViewInfo(ball.getPos(), ball.getRadius()));
        }
        return new GameStateViewInfo(balls, humanScore.get(), botScore.get());
    }

}
