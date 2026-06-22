package pcd.pooolTaskOriented.model;

import pcd.pooolTaskOriented.util.AtomicReference;
import pcd.pooolTaskOriented.util.AtomicReferenceImpl;
import pcd.pooolTaskOriented.view.GameStateViewInfo;

public class GameState {

    private final AtomicReference<Integer> humanScore = new AtomicReferenceImpl<>(0);
    private final AtomicReference<Integer> botScore = new AtomicReferenceImpl<>(0);
    private final AtomicReference<Boolean> gameOver = new AtomicReferenceImpl<>(false);
    private final AtomicReference<String> gameResult = new AtomicReferenceImpl<>("");

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
        return new GameStateViewInfo(humanScore.get(), botScore.get());
    }

}
